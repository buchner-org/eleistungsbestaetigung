package de.yoshteq.dcs.fhir;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IBase;

import ca.uhn.fhir.context.FhirContext;

public record TestStringWriter() {

    private static FhirContext ctx = FhirContext.forR4();

    public static String toString(IBaseResource resource){
		return ctx.newJsonParser().encodeResourceToString(resource);
    }

    public static String toString(IBase dataType) {
        return ctx.newJsonParser().encodeToString(dataType);
    }

}
