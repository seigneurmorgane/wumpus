package eu.su.mas.dedaleEtu.mas.behaviours.explo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class SendInfosBehaviour extends OneShotBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3429744247766338802L;
	private List<Couple<String,List<Couple<Observation,Integer>>>> closedNodes;
	private List<String> openNodes;
	private List<Integer> comp = new ArrayList<Integer>();
	private List<Couple<String,String>> Edges;
	private List<String> path;

	public SendInfosBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, List<Couple<String,List<Couple<Observation,Integer>>>> closedNodes, List<Couple<String,String>> Edges, List<String> openNodes,List<String> path) {
		super(myagent);
		this.closedNodes = closedNodes;
		this.openNodes = openNodes;
		this.comp.add(0);
		this.Edges = Edges;
		this.path = path;
	}

	@Override
	public void action() {
		if(path.size() > 0)
			this.comp.add(path.size());
		else {
			Random rand = new Random();
			this.comp.add(Math.abs(rand.nextInt()*100+rand.nextInt()*10+rand.nextInt()));
		}
		System.out.println(this.myAgent.getLocalName()+" my open nodes : "+this.openNodes);
		System.out.println("mes noeuds fermés à envoyer :"+closedNodes);
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Explo");
		dfd.addServices(sd);
		DFAgentDescription[] result;

		try {
			result = DFService.search(this.myAgent, dfd);
			if (result == null || result.length <= 0) {
				System.out.println("Search returns null");
			} else {
				for (DFAgentDescription dfd_res : result) {
					if (!dfd_res.getName().getLocalName().equals(this.myAgent.getLocalName())) {
						// 1°Create the message
						final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						msg.setSender(this.myAgent.getAID());
						msg.addReceiver(new AID(dfd_res.getName().getLocalName(), AID.ISLOCALNAME));
						Couple<List<Couple<String,String>>,List<Integer>> tmp = new Couple<>(Edges,comp);
						Couple<List<Couple<String,List<Couple<Observation,Integer>>>>,Couple<List<Couple<String,String>>,List<Integer>>> infos = new Couple<>(this.closedNodes,tmp);

						// 2° compute the random value
						msg.setContentObject((Couple<List<Couple<String,List<Couple<Observation,Integer>>>>,Couple<List<Couple<String,String>>,List<Integer>>>)infos);
						((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
						System.out.println(this.myAgent.getLocalName() + " sent my infos to " + dfd_res.getName().getLocalName());
						System.out.println("backpack = 0\n lenpath = "+this.comp.get(1));
					}
				}
			}
		} catch (FIPAException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	public int onEnd() {
		return 5;
	}

}
