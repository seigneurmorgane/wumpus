package eu.su.mas.dedaleEtu.mas.agents.dummies;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.*;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.Behaviour;

/**
 * ExploreSolo agent. It explore the map using a DFS algorithm. It stops when
 * all nodes have been visited
 * 
 * 
 * @author hc
 *
 */

public class ExploreSoloAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -6431752665590433727L;
	private MapRepresentation myMap = null;
	private Set<String> closedNodes = new HashSet<String>();
	private List<String> openNodes = new ArrayList<String>();
	private List<Couple<String,String>> edge = new ArrayList<>();
	private List<Couple<String,String>> otherEdge = new ArrayList<>();
	private Set<String> otherClosedNodes = new HashSet<String>();

	/**
	 * This method is automatically called when "agent".start() is executed.
	 * Consider that Agent is launched for the first time. 1) set the agent
	 * attributes 2) add the behaviours
	 * 
	 */
	protected void setup() {

		super.setup();

		List<Behaviour> lb = new ArrayList<Behaviour>();

		FSMBehaviour fsm = new FSMBehaviour(this) {
			public int onEnd() {
				System.out.println("FSM behaviour terminé");
				myAgent.doDelete();
				return super.onEnd();
			}

		};
		
		/************************************************
		 * 
		 * ADD the behaviours
		 * 
		 ************************************************/

		// definiton des etats
		fsm.registerFirstState(new InitDFBehaviour(this, "Explo"), "DF");
		fsm.registerState(new ExploSoloBehaviour(this, this.myMap, this.closedNodes ,this.otherClosedNodes,this.edge,this.otherEdge), "Dep");
		fsm.registerState(new SendPingBehaviour(this), "SPing");
		fsm.registerState(new ReceivePingBehaviour(this), "RPing");
		fsm.registerState(new SendClosedNodeBehaviour(this, this.closedNodes), "SNode");
		fsm.registerState(new ReceiveNodeClosedBehaviour(this, this.otherClosedNodes), "RNode");
		fsm.registerState(new SendEdgeBehaviour(this,this.edge), "SEdge");
		fsm.registerState(new ReceiveEdgeBehaviour(this,this.otherEdge), "REdge");
		fsm.registerLastState(new EndBehaviour(this), "End");

		// definition des transaction
		fsm.registerDefaultTransition("DF", "Dep");
		fsm.registerTransition("Dep", "SPing",2);
		fsm.registerTransition("SPing", "RPing", 3);
		fsm.registerTransition("RPing", "RPing", 3);	
		fsm.registerTransition("RPing", "SNode", 4);
		fsm.registerTransition("SNode", "RNode", 5);
		fsm.registerTransition("RNode", "RNode", 5);
		fsm.registerTransition("RNode", "SEdge",6);
		fsm.registerTransition("SEdge","REdge",7);
		fsm.registerTransition("REdge","REdge",7);
		fsm.registerTransition("REdge","Dep",1);
		fsm.registerTransition("Dep", "End",8);

		lb.add(fsm);
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */

		addBehaviour(new startMyBehaviours(this, lb));

		System.out.println("the  agent " + this.getLocalName() + " is started");

	}

}