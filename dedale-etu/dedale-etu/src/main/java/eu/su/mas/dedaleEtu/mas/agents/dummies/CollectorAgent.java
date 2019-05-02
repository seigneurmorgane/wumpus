package eu.su.mas.dedaleEtu.mas.agents.dummies;

import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.EndBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.InitDFBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.collect.*;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

import java.util.ArrayList;
import java.util.List;

import dataStructures.tuple.Couple;

import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.Behaviour;

public class CollectorAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = -1784844593772918359L;


	private MapRepresentation myMap;
	private List<Couple<String, List<Couple<Observation, Integer>>>> closedNodes = new ArrayList<Couple<String, List<Couple<Observation, Integer>>>>();
	private List<String> openNodes = new ArrayList<String>();
	private List<Couple<String, String>> Edges = new ArrayList<Couple<String, String>>();

	private List<Couple<String, String>> otherEdges = new ArrayList<Couple<String, String>>();
	private List<Couple<String, List<Couple<Observation, Integer>>>> otherClosedNodes = new ArrayList<Couple<String, List<Couple<Observation, Integer>>>>();

	private List<String> path = new ArrayList<String>();
	private List<String> otherOpenNodes = new ArrayList<String>();

	private List<Couple<Integer,Couple<String,List<String>>>> otherPaths = new ArrayList<Couple<Integer,Couple<String,List<String>>>>();
	private List<Observation> type_tresor = new ArrayList<Observation>();
	private List<String> locationTanker = new ArrayList<String>();
	
	private List<Couple<String,String>> help = new ArrayList<>();

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
			private static final long serialVersionUID = 4338232456572336591L;

			public int onEnd() {
				System.out.println("FSM behaviour terminé");
				myAgent.doDelete();
				return super.onEnd();
			}
		};

		/************************************************
		 * 
		 * Define State
		 * 
		 ************************************************/
		
		fsm.registerFirstState(new InitDFBehaviour(this, "Collect"), "DF");
		fsm.registerState(new WalkBehaviour(this, this.myMap, this.closedNodes, this.otherClosedNodes,this.openNodes, this.Edges,this.otherEdges, this.path, this.otherPaths,this.otherOpenNodes,this.type_tresor,this.locationTanker,this.help), "Walk");
		fsm.registerState(new SendDatasBehaviour(this,this.closedNodes, this.Edges, this.path,this.openNodes),"Send");
		fsm.registerState(new ReceiveDatasBehaviour(this,this.path,this.otherPaths,this.otherClosedNodes,this.otherEdges,this.otherOpenNodes,this.myMap,this.locationTanker,this.help),"Receive");
		fsm.registerState(new HelpRequiredBehaviour(this,this.type_tresor, this.closedNodes,this.Edges,this.openNodes),"Help");
		fsm.registerLastState(new EndBehaviour(this), "End");
		

		/************************************************
		 * 
		 * Define Transition
		 * 
		 ************************************************/
		fsm.registerDefaultTransition("DF", "Walk");
		fsm.registerTransition("Walk", "Send",2);
		fsm.registerTransition("Walk", "Help", 4);
		fsm.registerTransition("Send", "Receive",3);
		fsm.registerTransition("Receive", "Walk",1);
		fsm.registerTransition("Help","Send",2);
		fsm.registerTransition("Walk","End",5);


		/************************************************
		 * 
		 * ADD the behaviours
		 * 
		 ************************************************/
		lb.add(fsm);

		addBehaviour(new startMyBehaviours(this, lb));

		System.out.println("the  agent " + this.getLocalName() + " is started");

	}

	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown() {

	}

}
