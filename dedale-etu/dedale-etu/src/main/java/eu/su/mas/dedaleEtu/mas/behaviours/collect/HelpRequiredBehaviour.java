package eu.su.mas.dedaleEtu.mas.behaviours.collect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class HelpRequiredBehaviour extends SimpleBehaviour{

	private static final long serialVersionUID = 6195172078037629487L;
	private List<Observation> type_tresor;
	private List<Couple<String,List<Couple<Observation,Integer>>>> closedNodes;
	private List<Couple<String,String>> Edges;
	private List<String> openNodes;
	private int attente = 0;
	private boolean finished = false;

	public HelpRequiredBehaviour(AbstractDedaleAgent myagent, List<Observation> type_tresor, List<Couple<String,List<Couple<Observation,Integer>>>> closedNodes,
			List<Couple<String,String>> Edges, List<String> openNodes){
		super(myagent);
		this.type_tresor=type_tresor;
	}


	@Override
	public void action() {
		this.finished=false;
		attente++;
		this.sendInfos();
		this.myAgent.doWait(100);
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		dfd.addServices(sd);
		DFAgentDescription[] result;

		try {
			result = DFService.search(this.myAgent, dfd);
			if (result == null || result.length <= 0) {
				System.out.println("Search returns null");
			} else {

				String myPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
				List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
				List<Integer> infos = new ArrayList<Integer>();
				int force = 0;
				int serrure = 0;
				boolean ouvert = false;
				int tresor = -1;
				for (Couple<Observation, Integer> o : lobs.get(0).getRight()) {
					if( ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace()>0) {
						switch (o.getLeft()) {
						case DIAMOND:
							if(type_tresor.contains(Observation.DIAMOND) && o.getRight()>0) {
								ouvert = ((AbstractDedaleAgent)this.myAgent).openLock(o.getLeft());
								if( ouvert ) {
									tresor =((AbstractDedaleAgent) this.myAgent).pick();
									System.out.println("collecté : "+tresor);
								}
								else
									tresor = 0;
							}
							break;
						case GOLD:
							if(type_tresor.contains(Observation.GOLD) && o.getRight()>0) {
								ouvert = ((AbstractDedaleAgent)this.myAgent).openLock(o.getLeft());
								if(  ouvert) {
									tresor = ((AbstractDedaleAgent) this.myAgent).pick();
									System.out.println("collecté "+tresor);
								}
								else
									tresor = 0;
							}

							break;
						case LOCKPICKING:
							serrure = o.getRight();
							break;
						case STRENGH:
							force = o.getRight();
							break;
						default:
							break;
						}
					}
				}




				if (!ouvert) {
					for(Couple<Observation,Integer> obs : ((AbstractDedaleAgent)this.myAgent).getMyExpertise()) {
						switch(obs.getLeft()) {
						case LOCKPICKING:
							serrure-= obs.getRight();
							break;
						case STRENGH:
							force-= obs.getRight();
							break;
						default:
							break;
						}
					}

					infos.add(serrure);
					infos.add(force);

					for (DFAgentDescription dfd_res : result) {
						if (!dfd_res.getName().getLocalName().equals(this.myAgent.getLocalName())) {
							// 1°Create the message
							final ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
							msg.setSender(this.myAgent.getAID());
							msg.addReceiver(new AID(dfd_res.getName().getLocalName(), AID.ISLOCALNAME));
							// 2° compute the random value
							Couple<String,List<Integer>> to_send = new Couple<String,List<Integer>>(myPosition,infos);
							
							msg.setContentObject((Couple<String,List<Integer>>) to_send);
							((AbstractDedaleAgent) this.myAgent).sendMessage(msg);
						}

					}
					//this.attente++;

				}
				if(ouvert || this.attente > 20) {
					this.finished = true;
					this.attente = 0;
				}


			}

		}catch (FIPAException | IOException e) {
			e.printStackTrace();
		}


	}

	@Override
	public boolean done() {
		return finished;
	}


	public int onEnd() {
		return 2;
	}

	public void sendInfos() {
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


}
