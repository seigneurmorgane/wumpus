package eu.su.mas.dedaleEtu.mas.behaviours.collect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class SendDatasBehaviour extends OneShotBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8316397052697657737L;
	private List<Couple<String,List<Couple<Observation,Integer>>>> closedNodes;
	private List<Couple<String,String>> Edges;
	private List<String> path;
	private List<String> openNodes;
	
	public SendDatasBehaviour(AbstractDedaleAgent myagent,List<Couple<String,List<Couple<Observation,Integer>>>> closedNodes,
			List<Couple<String,String>> Edges, List<String> path, List<String> openNodes) {
		super(myagent);
		this.closedNodes = closedNodes;
		this.Edges = Edges;
		this.path = path;
		this.openNodes = openNodes;
	}
	
	@Override
	public void action() {
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		dfd.addServices(sd);
		DFAgentDescription[] result;
		
		String myPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		try {
			result = DFService.search(this.myAgent, dfd);
			if (result == null || result.length <= 0) {
				System.out.println("Search returns null");
			} else {
				
				List<String> tmp = new ArrayList<String>();
				for(String n : this.path)
					tmp.add(n);
				if(tmp.size()> 0 && !(tmp.get(0).equals(myPosition))) {
					tmp.add(0,myPosition);
				}
				if(tmp.size() == 0) 
					tmp.add(myPosition);
				// les priorités
				Couple<String,List<String>> prio = new Couple<String,List<String>>(((AbstractDedaleAgent)this.myAgent).getLocalName(),tmp);
				int sac = ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace();
				Couple<Integer,Couple<String,List<String>>> tmp2 = new Couple<Integer,Couple<String,List<String>>>(sac,prio);
				// les aretes + priorité
				Couple<List<Couple<String,String>>,Couple<Integer,Couple<String,List<String>>>> aretes = new Couple<List<Couple<String,String>>,Couple<Integer,Couple<String,List<String>>>>(this.Edges,tmp2);
				
				// les noeuds ouverts + aretes + priorité
				Couple<List<String>,Couple<List<Couple<String,String>>,Couple<Integer,Couple<String,List<String>>>>> ouverts = new Couple<List<String>,Couple<List<Couple<String,String>>,Couple<Integer,Couple<String,List<String>>>>>(this.openNodes,aretes);
				
				// les noeuds fermés + ouverts + aretes + priorité
				Couple<List<Couple<String,List<Couple<Observation,Integer>>>>,Couple<List<String>,Couple<List<Couple<String,String>>,Couple<Integer,Couple<String,List<String>>>>>> infos = 
						new Couple<List<Couple<String,List<Couple<Observation,Integer>>>>,Couple<List<String>,Couple<List<Couple<String,String>>,Couple<Integer,Couple<String,List<String>>>>>>(this.closedNodes,ouverts);
				
				for (DFAgentDescription dfd_res : result) {
					if (!dfd_res.getName().getLocalName().equals(this.myAgent.getLocalName())) {
						// 1°Create the message
						
								
						
						
						final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						
						msg.setContentObject(
								(Couple<List<Couple<String,List<Couple<Observation,Integer>>>>,Couple<List<String>,Couple<List<Couple<String,String>>,Couple<Integer,Couple<String,List<String>>>>>>)infos);
						
						
						msg.setSender(this.myAgent.getAID());
						msg.addReceiver(new AID(dfd_res.getName().getLocalName(), AID.ISLOCALNAME));
						
						// 2° compute the random value
						
						((AbstractDedaleAgent) this.myAgent).sendMessage(msg);

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
		return 3;
	}

}

