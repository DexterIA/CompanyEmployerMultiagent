package dexter;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class CompanyAgent extends Agent {

    public class Post {
        int rating;
        int old;
        int salary;
        boolean enabled;

        Post(int r,int o,int s) {
            rating = r;
            old = o;
            salary = s;
            enabled = true;
        }
    }

    private Post[] posts;
    private int counter;
    // The GUI by means of which the user can add books in the catalogue
    private AddPostGui myGui;

    // Put agent initializations here
    protected void setup() {

        posts = new Post[1000];
        counter = 0;

        // Create and show the GUI
        myGui = new AddPostGui(this);
        myGui.showGui();

        // Register the company-employer service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("company");
        sd.setName("JADE-company-employer");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Add the behaviour serving queries from buyer agents
        addBehaviour(new OfferRequestsServer());

    }

    // Put agent clean-up operations here
    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Close the GUI
        myGui.dispose();
        // Printout a dismissal message
        System.out.println("CompanyAgent "+getAID().getName()+" terminating.");
    }

    /**
     This is invoked by the GUI when the user adds a new post
     */
    public void updateCatalogue(final int rating, final int old, final int salary) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                posts[counter] = new Post(rating, old, salary);
                counter++;
                System.out.println("Post was inserted with params:\n Rating: " + rating +
                "\n Age limit: " + old + "\n Salary: " + salary);
            }
        } );
    }

    /**
     Inner class OfferRequestsServer.
     This is the behaviour used by Employer agents to serve incoming requests
     for offer from buyer agents.
     If the requested post is in the local catalogue the company agent replies
     with a PROPOSE message with allowed or denied.
     */
    private class OfferRequestsServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // CFP Message received. Process it
                String params = msg.getContent();
                ACLMessage reply = msg.createReply();

                String[] parts = params.split("-");
                String name = parts[0];
                int rating = Integer.parseInt(parts[1]);
                int age = Integer.parseInt(parts[2]);

                for (int i = 0; i < counter; i++) {
                    if (posts[i].enabled) {
                        if (posts[i].rating <= rating && posts[i].old >= age) {
                            // The requested book is available for sale. Reply with the price
                            reply.setPerformative(ACLMessage.PROPOSE);
                            reply.setContent("Allowed");
                            System.out.println("Employer " + name + " was employed");
                            break;
                        } else {
                            // The requested book is NOT available for sale.
                            reply.setPerformative(ACLMessage.REFUSE);
                            reply.setContent("Denied");
                            System.out.println("Employer " + name + " does not fit");
                            break;
                        }
                    }
                }
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }  // End of inner class OfferRequestsServer

}