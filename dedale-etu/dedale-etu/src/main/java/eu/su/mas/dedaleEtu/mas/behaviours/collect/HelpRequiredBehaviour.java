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
	private int attente = 0;
	private boolean finished = false;

	public HelpRequiredBehaviour(AbstractDedaleAgent myagent) {
		super(myagent);
	}


	@Override
	public void action() {

		attente++;
		System.out.println(attente);
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
				//Observation tr_type = ((AbstractDedaleAgent)this.myAgent).getMyTreasureType();
				for (Couple<Observation, Integer> o : lobs.get(0).getRight()) {
					if( ((AbstractDedaleAgent)this.myAgent).getBackPackFreeSpace()>0) {
						switch (o.getLeft()) {
						case DIAMOND:
							break;
						case GOLD:
							ouvert = ((AbstractDedaleAgent)this.myAgent).openLock(o.getLeft());
							if(ouvert) {
								int tresor = ((AbstractDedaleAgent)this.myAgent).pick();
								System.out.println("collecté "+tresor);
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
					this.attente++;

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
	

}
