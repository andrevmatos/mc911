package llvmast;
public  class LlvmBranch extends LlvmInstruction{
	public LlvmLabelValue label;
	public LlvmValue cond;
	public LlvmLabelValue brTrue;
	public LlvmLabelValue brFalse;
	

    public LlvmBranch(LlvmLabelValue label){
		this.label = label;
    }
    
    public LlvmBranch(LlvmValue cond,  LlvmLabelValue brTrue, LlvmLabelValue brFalse){
		this.cond = cond;
		this.brTrue = brTrue;
		this.brFalse = brFalse;
    }

    public String toString(){
		//verify if the variable label is NULL or not and return the correct value
    	if (label != null){
			return "br label %" + label;
		}
		else return "br i1 " + cond + ", label %" + brTrue + ", label %" + brFalse;
    }
}