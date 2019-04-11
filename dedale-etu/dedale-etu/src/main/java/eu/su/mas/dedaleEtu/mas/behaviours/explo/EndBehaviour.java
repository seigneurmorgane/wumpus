package eu.su.mas.dedaleEtu.mas.behaviours.explo;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.OneShotBehaviour;

public class EndBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = -4938320104666159831L;

	public EndBehaviour(final AbstractDedaleAgent myagent) {
		super(myagent);
	}

	@Override
	public void action() {
		System.out.println("Exploration terminer");
	}
	
	public int onEnd() {
		return 1;
	}

}
