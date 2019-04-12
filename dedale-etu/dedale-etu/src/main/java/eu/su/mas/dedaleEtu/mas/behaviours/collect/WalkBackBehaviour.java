package eu.su.mas.dedaleEtu.mas.behaviours.collect;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.OneShotBehaviour;

/**
 * This behaviour is triggerable every 600ms. It tries all the API methods at
 * each time step, store the treasure that match the entity treasureType in its
 * backpack and intends to empty its backPack in the Tanker agent (if he is in
 * reach) <br/>
 *
 * Rmq : This behaviour is in the same class as the DummyCollectorAgent for
 * clarity reasons. You should prefer to save your behaviours in the behaviours
 * package, and all the behaviours referring to a given protocol in the same
 * class
 * 
 * @author hc
 */
public class WalkBackBehaviour extends OneShotBehaviour {
	/**
	 * When an agent choose to move
	 * 
	 */
	private static final long serialVersionUID = 9088209402507795289L;
	private Couple<String, List<Couple<Observation, Integer>>> nextNode;

	public WalkBackBehaviour(final AbstractDedaleAgent myagent,
			Couple<String, List<Couple<Observation, Integer>>> nextNode) {
		super(myagent);
		this.nextNode = nextNode;
	}

	@Override
	public void action() {
		// Example to retrieve the current position
		String myPosition = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();

		if (myPosition != "") {
			List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent)
					.observe();

			// Little pause to allow you to follow what is going on
			try {
				this.myAgent.doWait(500);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Random move from the current position
			Random r = new Random();
			lobs.remove(this.nextNode);
			int moveId = 1 + r.nextInt(lobs.size() - 1);// removing the current position from the list of target to
														// accelerate the tests, but not necessary as to stay is an
														// action
			// The move action (if any) should be the last action of your behaviour
			((AbstractDedaleAgent) this.myAgent).moveTo(lobs.get(moveId).getLeft());
		}

	}

	@Override
	public int onEnd() {
		return 7;
	}

}