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
    	String s = "  " + lhs + " = icmp ";
		switch (conditionCode){
			case 1:
				s += "eq";
				break;
			case 2:
				s += "ne";
				break;
			case 3:
				s += "ugt";
				break;
			case 4:
				s += "uge";
				break;
			case 5:
				s += "ult";
				break;
			case 6:
				s += "ule";
				break;
			case 7:
				s += "sgt";
				break;
			case 8:
				s += "sge";
				break;
			case 9:
				s += "slt";
				break;
			case 10:
				s += "sle";
				break;
			default:
				break;
		}
		s += " " + type + " " + op1 + ", " + op2;
		return s;
    }
}