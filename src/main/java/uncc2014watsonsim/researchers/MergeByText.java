package uncc2014watsonsim.researchers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import uncc2014watsonsim.Answer;
import uncc2014watsonsim.Question;
import uncc2014watsonsim.nlp.Environment;
import uncc2014watsonsim.nlp.Synonyms;

public class MergeByText extends Researcher {
	private final Synonyms syn;
	private final Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * Create a new merger using shared environment resources.
	 * @param env
	 */
	public MergeByText(Environment env) {
		syn = new Synonyms(env);
	}
	
	@Override
	/** Call merge on any two answers with the same title */
	public void question(Question q) {
		List<List<Answer>> answer_blocks = new ArrayList<>();
		// Arrange the answers into blocks
		each_answer:
		for (Answer original : q) {
			for (List<Answer> block : answer_blocks) {
				for (Answer example : block) {
					// Look through the examples in this topic
					// If it matches, choose to put it in this block and quit.
					if (syn.matchViaLevenshtein(original.candidate_text, example.candidate_text)) {
						block.add(original);
						continue each_answer;
					}
				}
			}
			
			// Make a new topic for this answer
			List<Answer> new_block = new ArrayList<>();
			new_block.add(original);
			answer_blocks.add(new_block);
		}

		// Merge the blocks
		final int prev_answers = q.size();
		q.clear();
		for (List<Answer> block : answer_blocks) {
			if (block.size() > 1) {
				q.add(Answer.merge(block));
			} else {
				q.add(block.get(0));
			}
		}
		
		log.info("Merged " + prev_answers + " candidates into " + q.size() + " (by surface similarity).");
	}
}