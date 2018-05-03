package sjdb;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class Estimator implements PlanVisitor {

	private int cost = 0;
	public Estimator() {
		// empty constructor
	}
	/* 
	 * Create output relation on Scan operator
	 *
	 * Example implementation of visit method for Scan operators.
	 */
	public void visit(Scan op) {
		Relation input = op.getRelation();
		Relation output = new Relation(input.getTupleCount());
		
		Iterator<Attribute> iter = input.getAttributes().iterator();
		while (iter.hasNext()) {
			output.addAttribute(new Attribute(iter.next()));
		}
		op.setOutput(output);
		//count the query cost for scan
		cost = cost + output.getTupleCount();
	}
	public void visit(Project op) {
		Relation input = op.getInput().getOutput();
		//get the tuple counts
		Relation output = new Relation(input.getTupleCount());
		//get the names
		List<Attribute> attributes = op.getAttributes();
		List<String> attributesNames = new ArrayList<String>();
		for(Attribute attr: attributes){
			attributesNames.add(attr.getName());
		}
		//project the specific attributes by name
		Iterator<Attribute> iter = input.getAttributes().iterator();
		while (iter.hasNext()) {
			Attribute attribute = new Attribute(iter.next());
			if(attributesNames.contains(attribute.getName())) {
				output.addAttribute(attribute);
			}
		}
		op.setOutput(output);
		//count the query cost for project
		cost = cost + output.getTupleCount();
	}
	public void visit(Select op) {
		Relation input = op.getInput().getOutput();
		Predicate predicate = op.getPredicate();
		Attribute leftAttribute = predicate.getLeftAttribute();
		Attribute rightAttribute = predicate.getRightAttribute();
		int max_count, min_count;
		
		if(predicate.equalsValue()){
			Attribute leftAttr = input.getAttribute(leftAttribute);

			//calculate the tuple count and generate the output based on that
			Relation output = new Relation(input.getTupleCount()/leftAttr.getValueCount());

			//assign the attributes to only the corresponding output 
			Iterator<Attribute> iter = input.getAttributes().iterator();
			while (iter.hasNext()) {
				Attribute attr = new Attribute(iter.next());
				if(attr.equals(leftAttr))
					output.addAttribute(new Attribute(attr.getName(), 1));
				else
					output.addAttribute(attr);
			}
			
			op.setOutput(output);
			//count the query cost for selection
			cost = cost + output.getTupleCount();
		
		} 
		else {
			Attribute leftAttr = input.getAttribute(leftAttribute);
			Attribute rightAttr = input.getAttribute(rightAttribute);
			
			//calculate the tuple count and generate the output based on that
			max_count = Math.max(leftAttr.getValueCount(),rightAttr.getValueCount());
			Relation output = new Relation(input.getTupleCount()/max_count);
			
			//assign the attributes to only the corresponding output 
			min_count = Math.min(leftAttr.getValueCount(), rightAttr.getValueCount());
			Iterator<Attribute> iter = input.getAttributes().iterator();
			while (iter.hasNext()) {
				Attribute attr = new Attribute(iter.next());
				if((attr.equals(leftAttr)) || (attr.equals(rightAttr)))
					output.addAttribute(new Attribute(attr.getName(), min_count));
				else
					output.addAttribute(attr);
			}
			op.setOutput(output);	
			//count the query cost for selection
			cost = cost + output.getTupleCount();
		}	
	}
	public void visit(Product op) {
		
		Relation LeftInput = op.getLeft().getOutput();
		Relation RightInput = op.getRight().getOutput();

		//the tuple of the output is the product of two costs.
		Relation output = new Relation(LeftInput.getTupleCount() * RightInput.getTupleCount());

		//attributes with initial relations 
		Iterator<Attribute> Leftiter = LeftInput.getAttributes().iterator();
		Iterator<Attribute> Rightiter = RightInput.getAttributes().iterator();
		while (Leftiter.hasNext()) {
			output.addAttribute(new Attribute(Leftiter.next()));
		}
		while (Rightiter.hasNext()) {
			output.addAttribute(new Attribute(Rightiter.next()));
		}
		
		op.setOutput(output);
		//count the query cost for selection
		cost = cost + output.getTupleCount();
	}
	public void visit(Join op) {
		int max_count, min_count;
		Relation LeftInput = op.getLeft().getOutput();
		Relation RightInput = op.getRight().getOutput();
		
		//get the predicate attributes and input relations attributes
		Predicate predicate = op.getPredicate();
		
		Attribute leftAttribute = predicate.getLeftAttribute();
		Attribute LeftAttr = LeftInput.getAttribute(leftAttribute);
		
		Attribute rightAttribute = predicate.getRightAttribute();
		Attribute RightAttr = RightInput.getAttribute(rightAttribute);

		//calculate the tuple count and generate the output based on that
		max_count = Math.max(LeftAttr.getValueCount(), RightAttr.getValueCount());
		Relation output = new Relation((LeftInput.getTupleCount() * RightInput.getTupleCount())/max_count);
		
		//attributes with initial relations 
		//with some changes to the value counts 
		Iterator<Attribute> Leftiter = LeftInput.getAttributes().iterator();
		min_count = Math.min(LeftAttr.getValueCount(), RightAttr.getValueCount());
		while (Leftiter.hasNext()) {
			Attribute attr = new Attribute(Leftiter.next());
			if(attr.equals(LeftAttr))
				output.addAttribute(new Attribute(attr.getName(), min_count));
			else
				output.addAttribute(attr);
		}
		
		Iterator<Attribute> Rightiter = RightInput.getAttributes().iterator();
		while (Rightiter.hasNext()) {
			Attribute attr = new Attribute(Rightiter.next());
			if(attr.equals(RightAttr))
				output.addAttribute(new Attribute(attr.getName(), Math.min(LeftAttr.getValueCount(), RightAttr.getValueCount())));
			else
				output.addAttribute(attr);
		}
		op.setOutput(output);
		//count the query cost for selection
		cost = cost + output.getTupleCount();
	}
	public int getCost(Operator plan) {
		this.cost = 0;
		plan.accept(this);		
		return this.cost;
	}
}
