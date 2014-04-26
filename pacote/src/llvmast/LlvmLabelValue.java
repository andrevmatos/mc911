package llvmast;
public class LlvmLabelValue extends LlvmValue{
	public String value;
	static private int ord = 0;
	public LlvmLabelValue(String value){
		type = LlvmPrimitiveType.LABEL;
		this.value = value+"_"+ord++;
	}

	public String toString(){
		return ""+ value;
	}
}