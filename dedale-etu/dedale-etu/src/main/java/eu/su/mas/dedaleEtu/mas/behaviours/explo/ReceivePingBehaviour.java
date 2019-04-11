package eu.su.mas.dedaleEtu.mas.behaviours.explo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ReceivePingBehaviour extends SimpleBehaviour{

	private static final long serialVersionUID = 8567659731496787061L;
	private boolean finished = false;
	private int trans = 3;
	private MapRepresentation myMap;
	private List<String> openNodes = new ArrayList<String>();
	private List<String> path;
	private int nb = 0;
	private String nom_corres;
	
	public ReceivePingBehaviour(final AbstractDedaleAgent myagent,MapRepresentation myMap, List<String> openNodes,List<String> path, String nom_corres) {
		super(myagent);
		this.myMap = myMap;
		this.openNodes = openNodes;
		this.path = path;
		this.nom_corres = nom_corres;
	}
	
	@Override
	public void action() {
		this.myAgent.doWait(500);
		System.out.println("essai numero "+(nb++));
		System.out.println(this.myAgent.getLocalName()+" is waiting for another ping ...\n current open nodes : "+this.openNodes);
		System.out.println("il y a t il un path précis ?");
		if(this.path.size()> 0)
			System.out.println(this.path);
		//1) receive the message
		final MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);			

		final ACLMessage msg = this.myAgent.receive(msgTemplate);
		try {
			if (msg != null && ((String)(msg.getContent())).charAt(0) == 'a') {
				this.nom_corres = msg.getContent().substring(1);
				this.finished = true;
				this.trans = 4;
				this.nb = 0;
				System.out.println(this.myAgent.getLocalName()+" <----Ping received from "+msg.getSender().getLocalName()+" ,content= "+msg.getContent());
			}else if(nb>4){/*
				if(msg == null)
					System.out.println("pas de message");
				else 
					System.out.println("le message n'est pas un ping : "+msg.getContent());
				this.trans = 1;
				this.finished=true;
				System.out.println(this.openNodes.size());
				String nextNode = null;
				if (path.size()>0) {
					if(path.get(0).equals(((AbstractDedaleAgent)this.myAgent).getCurrentPosition())) {
						path.remove(path.get(0));
						if (path.size() == 0) {
							if(this.openNodes.size()> 0) {
								this.path = myMap.getShortestPath(((AbstractDedaleAgent)this.myAgent).getCurrentPosition(), this.openNodes.get(0));
							} else
								this.path.add( ((AbstractDedaleAgent)this.myAgent).observe().get(1).getLeft());
						}
					}
					nextNode = path.get(0);
					System.out.println(this.myAgent.getLocalName()+" go to old dest"+nextNode);
					System.out.println("my pos = "+((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
					System.out.println("my path = "+this.path);
				}
				else {
					System.out.println("no path !");
					if(this.openNodes.size()> 0) {
						this.path = myMap.getShortestPath(((AbstractDedaleAgent)this.myAgent).getCurrentPosition(), this.openNodes.get(0));
						nextNode = this.path.get(0);
					} else {
						nextNode = ((AbstractDedaleAgent)this.myAgent).observe().get(1).getLeft();
						this.path.clear();
						this.path.add(nextNode);
					}

					System.out.println("go to new dest"+nextNode);
				}
				this.myAgent.doWait(500);
				try {
					if( ! ((AbstractDedaleAgent)this.myAgent).moveTo(nextNode)) {
						this.trans = 3;
					} 
				} catch(RuntimeException e) {
					e.printStackTrace();
					System.out.println("nextNode = "+nextNode);
					System.out.println("myPosition = "+((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
					this.path = myMap.getShortestPath(((AbstractDedaleAgent)this.myAgent).getCurrentPosition(), nextNode);
					nextNode = this.path.get(0);
					if( ! ((AbstractDedaleAgent)this.myAgent).moveTo(nextNode)) {
						this.trans = 3;
					}
				}*/
				
			// deuxième solution : ceci est appelé lorsque l'agent ne reçoit tjrs pas de ping
			// il doit donc avancer
				String nextNode = null;
				if(path.size()> 0) {
					System.out.println("le chemin est bien enregistré : "+this.path);
					//nextNode = path.get(0);
				} else if (this.openNodes.size()>0){
					this.path = myMap.getShortestPath(((AbstractDedaleAgent)this.myAgent).getCurrentPosition(), this.openNodes.get(0));
				} else {
					this.path.add(((AbstractDedaleAgent)this.myAgent).observe().get(1).getLeft());
				}
				
				nextNode = path.get(0);
				this.nb = 0;
				this.trans = 1;
				this.finished=true;
				System.out.println(this.myAgent.getLocalName()+"("+((AbstractDedaleAgent)this.myAgent).getCurrentPosition()+") va essayer d'aller en "+nextNode);
				if(!((AbstractDedaleAgent)this.myAgent).moveTo(nextNode)) {
					System.out.println("je n'arrive pas à bouger !!!");
					this.trans = 3;
				}
				
			
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public boolean done() {
		return this.finished;
	}
	
	@Override
	public int onEnd() {
		return this.trans;
	}
}
