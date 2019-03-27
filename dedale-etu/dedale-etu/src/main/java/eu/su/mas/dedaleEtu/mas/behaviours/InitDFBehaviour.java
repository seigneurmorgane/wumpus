package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;


public class InitDFBehaviour extends OneShotBehaviour{
	
	private static final long serialVersionUID = 9088209402507795288L;
	
	private final String type;
	
	public InitDFBehaviour(final Agent myagent,String type) {
		super(myagent);
		this.type = type;
	}

	@Override
	public void action() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(this.myAgent.getAID());
		
		ServiceDescription sd = new ServiceDescription();
		sd.setType(type);
		sd.setName(this.myAgent.getLocalName());
		
		dfd.addServices(sd);
		
		try {
			DFService.register(this.myAgent, dfd);
		}catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}
}
