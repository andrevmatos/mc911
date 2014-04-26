package llvmast;
public  class LlvmIcmp extends LlvmInstruction{
	LlvmRegister lhs;
	int conditionCode; 
	LlvmType type;
	LlvmValue op1; 
	LlvmValue op2;
	
    public LlvmIcmp(LlvmRegister lhs,  int conditionCode, LlvmType type, LlvmValue op1, LlvmValue op2){
    	this.lhs = lhs;
    	this.conditionCode = conditionCode;
    	this.type = type;
    	this.op1 = op1;
    	this.op2 = op2;	
    }

    public String toString(){
		switch (conditionCode){
			case 1:
				return "  " + lhs + "= icmp eq" + type + op1 + ", " + op2;
			case 2:
				return "  " + lhs + "= icmp ne" + type + op1 + ", " + op2;
			case 3:
				return "  " + lhs + "= icmp ugt" + type + op1 + ", " + op2;
			case 4:
				return "  " + lhs + "= icmp uge" + type + op1 + ", " + op2;
			case 5:
				return "  " + lhs + "= icmp ult" + type + op1 + ", " + op2;
			case 6:
				return "  " + lhs + "= icmp ule" + type + op1 + ", " + op2;
			case 7:
				return "  " + lhs + "= icmp sgt" + type + op1 + ", " + op2;
			case 8:
				return "  " + lhs + "= icmp sge" + type + op1 + ", " + op2;
			case 9:
				return "  " + lhs + "= icmp slt" + type + op1 + ", " + op2;
			case 10:
				return "  " + lhs + "= icmp sle" + type + op1 + ", " + op2;
			default: return " ";
		}
    }  
}