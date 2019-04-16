package eu.su.mas.dedaleEtu.mas.agents.dummies;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.explo.*;
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
	private List<Couple<String,List<Couple<Observation,Integer>>>> closedNodes = new ArrayList<Couple<String,List<Couple<Observation,Integer>>>>();
	private List<Couple<String,List<Couple<Observation,Integer>>>> otherClosedNodes = new ArrayList<Couple<String,List<Couple<Observation,Integer>>>>();
	private List<String> openNodes = new ArrayList<String>();
	private List<Couple<String,String>> Edges = new ArrayList<Couple<String,String>>();
	private List<Couple<String,String>> otherEdges = new ArrayList<Couple<String,String>>();
	private List<String> path = new ArrayList<String>();
	private List<String> nom_corres = new ArrayList<String>();
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
			/**
			 * 
			 */
			private static final long serialVersionUID = 4338232456572336591L;

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
		fsm.registerState(new ExploSoloBehaviour(this, this.myMap, this.closedNodes ,this.otherClosedNodes,this.Edges,this.otherEdges,this.openNodes,this.path), "Dep");
		fsm.registerState(new SendPingBehaviour(this), "SPing");
		fsm.registerState(new ReceivePingBehaviour(this,this.myMap, this.openNodes,this.path,this.nom_corres), "RPing");
		/*fsm.registerState(new SendClosedNodeBehaviour(this, this.closedNodes), "SNode");
		fsm.registerState(new ReceiveNodeClosedBehaviour(this, this.otherClosedNodes), "RNode");
		fsm.registerState(new SendEdgeBehaviour(this,this.edge), "SEdge");
		fsm.registerState(new ReceiveEdgeBehaviour(this,this.otherEdge), "REdge");
		fsm.registerState(new IsBlockedExploBehaviour(this,this.myMap,this.openNodes),"IBlock");
		fsm.registerState(new DeblockExploBehaviour(this,this.myMap,this.openNodes,this.closedNodes),"DBlock");*/
		fsm.registerState(new SendInfosBehaviour(this,this.myMap,this.closedNodes,this.Edges,this.openNodes,this.path,this.nom_corres),"SInfos");
		fsm.registerState(new ReceiveInfosBehaviour(this,this.otherClosedNodes,this.otherEdges,this.openNodes,this.path,this.nom_corres),"RInfos");
		//fsm.registerLastState(new EndBehaviour(this), "End");
		fsm.registerState(new EndBehaviour(this), "End");
		
		
		// definition des transaction
		fsm.registerDefaultTransition("DF", "Dep");
		fsm.registerTransition("Dep", "SPing",2);
		fsm.registerTransition("SPing", "RPing", 3);
		fsm.registerTransition("RPing", "RPing", 3);	
		fsm.registerTransition("RPing", "End", 8);	
		fsm.registerTransition("RPing", "Dep", 1);
		/*fsm.registerTransition("RPing", "SNode", 4);
		fsm.registerTransition("SNode", "RNode", 5);
		fsm.registerTransition("RNode", "RNode", 5);
		fsm.registerTransition("RNode", "SEdge",6);
		fsm.registerTransition("SEdge","REdge",7);
		fsm.registerTransition("REdge","REdge",7);
		fsm.registerTransition("REdge","IBlock",9);
		fsm.registerTransition("IBlock", "DBlock",10);
		fsm.registerTransition("DBlock", "DBlock",10);
		fsm.registerTransition("DBlock", "Dep",1);
		fsm.registerTransition("DBlock", "End",8);
		fsm.registerTransition("DBlock","SPing",2);*/
		fsm.registerTransition("RPing","SInfos",4);
		fsm.registerTransition("SInfos", "RInfos", 5);
		fsm.registerTransition("RInfos", "RInfos",5);
		fsm.registerTransition("RInfos", "End",8);
		fsm.registerTransition("RInfos", "Dep",1);
		fsm.registerTransition("RInfos", "SPing",2);
		fsm.registerTransition("Dep", "End",8);
		fsm.registerTransition("End", "Dep",1);
		//fsm.registerTransition(source, event);

		lb.add(fsm);
		/***
		 * MANDATORY TO ALLOW YOUR AGENT TO BE DEPLOYED CORRECTLY
		 */

		addBehaviour(new startMyBehaviours(this, lb));

		System.out.println("the  agent " + this.getLocalName() + " is started");

	}

}