package eu.su.mas.dedaleEtu.mas.agents.dummies;


import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.tank.*;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;

/**
 * Dummy Tanker agent. It does nothing more than printing what it observes every 10s and receiving the treasures from other agents. 
 * <br/>
 * Note that this last behaviour is hidden, every tanker agent automatically possess it.
 * 
 * @author hc
 *
 */
public class TankerAgent extends AbstractDedaleAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1784844593772918359L;



	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 
	 * 			1) set the agent attributes 
	 *	 		2) add the behaviours
	 *          
	 */
	protected void setup(){

		super.setup();
		
		List<Behaviour> lb = new ArrayList<Behaviour>();
		FSMBehaviour fsm = new FSMBehaviour(this) {

			/**
			 * 
			 */
			private static final long serialVersionUID = -768256229603748830L;

			public int onEnd() {
				System.out.println("FSM behaviour terminé");
				myAgent.doDelete();
				return super.onEnd();
			}

		};
		// definiton des etats
		fsm.registerFirstState(new RandomWalkBehaviour(this),"RWalk");
		fsm.registerState(new SendNodeBehaviour(this),"Send");
		
		// definition des transaction
		fsm.registerTransition("RWalk", "Send", 2);
		fsm.registerTransition("Send", "RWalk", 1);
		
		lb.add(fsm);
		addBehaviour(new startMyBehaviours(this, lb));
		System.out.println("the  agent " + this.getLocalName() + " is started");



	}
}