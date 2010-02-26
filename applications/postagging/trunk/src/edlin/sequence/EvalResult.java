package edlin.sequence;

public class EvalResult {
	
	public String claz;
	public String span;
	
	public EvalResult(String claz, String span) {
		this.span = span;
		this.claz = claz;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((claz == null) ? 0 : claz.hashCode());
		result = prime * result + ((span == null) ? 0 : span.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final EvalResult other = (EvalResult) obj;
		if (claz == null) {
			if (other.claz != null)
				return false;
		} else if (!claz.equals(other.claz))
			return false;
		if (span == null) {
			if (other.span != null)
				return false;
		} else if (!span.equals(other.span))
			return false;
		return true;
	}
	

}
