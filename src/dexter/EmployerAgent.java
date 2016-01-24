package dexter;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class EmployerAgent extends Agent {

    // The list of known company agents
    private AID[] companyAgents;

    private AddAnketGui myGui;

    // Put agent initializations here
    protected void setup() {
        // Printout a welcome message
        myGui = new AddAnketGui(this);
        myGui.showGui();

        // Register the company-employer service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("employer");
        sd.setName("JADE-employer");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }


    // Put agent clean-up operations here
    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Close the GUI
        myGui.dispose();

        // Printout a dismissal message
        System.out.println("Employer-agent " + getAID().getName() + " terminating.");
    }

    public void onAddedAnket(final String name, final int rating, final int age) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {

                System.out.println("New employer was added, his name - " + name + "\n his rating: " + rating + "\n and have " + age + " old");
                // Update the list of seller agents
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("company");
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    System.out.println("Found the following company agents:");
                    companyAgents = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        companyAgents[i] = result[i].getName();
                        System.out.println(companyAgents[i].getName());
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }

                // Perform the request
                myAgent.addBehaviour(new RequestPerformer(name, rating, age));
            }
        });
    }

    /**
     * Inner class RequestPerformer.
     * This is the behaviour used by Book-buyer agents to request seller
     * agents the target book.
     */
    private class RequestPerformer extends Behaviour {
        private int result = 0; // The counter of replies from seller agents
        private MessageTemplate mt; // The template to receive replies
        private int step = 0;
        private String data;
        public String name;

        RequestPerformer(String n, int r, int a) {
            name = n;
            data = n + "-" + Integer.toString(r) + "-" + Integer.toString(a);
        }

        public void action() {
            switch (step) {
                case 0:
                    // Send the cfp to all sellers
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < companyAgents.length; ++i) {
                        cfp.addReceiver(companyAgents[i]);
                    }
                    cfp.setContent(data);
                    cfp.setConversationId("employer-company");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);
                    // Prepare the template to get proposals
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("employer-company"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    // Receive all proposals/refusals from seller agents
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Reply received
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            // This is an offer
                            if (reply.getContent().equals("Allowed")) {
                                System.out.println("Yea! My name is " + name + " and I've got this job! :)");
                                result = 1;
                            } else {
                                System.out.println("Sucks! My name is " + name + " and I was refused :(");
                                result = 2;
                            }
                        }

                    } else {
                        block();
                    }
                    break;
            }
        }

        public boolean done() {
            return result !=0;
        }
    }
}
