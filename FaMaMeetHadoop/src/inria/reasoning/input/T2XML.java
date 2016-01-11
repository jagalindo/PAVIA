package inria.reasoning.input;


import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.FMPlainTextReader;
import es.us.isa.FAMA.models.FAMAfeatureModel.fileformats.XMLWriter;
import es.us.isa.FAMA.models.variabilityModel.VariabilityModel;

public class T2XML {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		FMPlainTextReader reader = new FMPlainTextReader();
		XMLWriter writer = new XMLWriter();
		VariabilityModel parseFile = reader.parseFile("inputs/main.ovm");
		System.out.println("Parsing finished");
		writer.writeFile("inputs/main.xml", parseFile);
	}

}
