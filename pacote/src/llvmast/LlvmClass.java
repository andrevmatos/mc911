package llvmast;

public class LlvmClass extends LlvmInstruction {
	public LlvmStructure st;
	public String name;
	public LlvmClass(String name, LlvmStructure st) {
		this.st = st;
		this.name = name;
	}

	public String toString() {
		return "%class." + name + " = type " + st.toString();
	}
}
