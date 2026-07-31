package de.yoshteq.dcs.fhir.model.billing;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Identifier;

import de.yoshteq.dcs.fhir.FhirBinaryFileHelper;
import de.yoshteq.dcs.fhir.MetaHelper;
import de.yoshteq.dcs.fhir.UuidHelper;

public class InvoiceContainerBundle
{

	private InvoiceContainerBundle()
	{
	}

	public static Bundle create(String invoiceNumber, List<Bundle> signedChargeItemBundles, List<Path> binaryFiles) throws IOException
	{
		var bundle = new Bundle();
		bundle.setMeta(MetaHelper.withProfileUrl("https://e-lb.de/fhir/StructureDefinition/KK_ELB_InvoiceContainerBundle"));
		bundle.setType(BundleType.COLLECTION);

		bundle.setIdentifier(new Identifier()
			.setSystem("https://e-lb.de/fhir/sid/KK_ELB_Belegnummer")
			.setValue(invoiceNumber));

		for (var signedBundle : signedChargeItemBundles)
		{
			bundle.addEntry(createBundleEntry(signedBundle));
		}

		for (var binaryFile : binaryFiles)
		{
			bundle.addEntry(createBinaryFileEntry(binaryFile));
		}

		return bundle;
	}

	private static BundleEntryComponent createBundleEntry(Bundle bundle)
	{
		return new BundleEntryComponent()
			.setFullUrl(UuidHelper.toUuidString(UUID.randomUUID()))
			.setResource(bundle);
	}

	private static BundleEntryComponent createBinaryFileEntry(Path binaryFile) throws IOException
	{
		return new BundleEntryComponent()
			.setFullUrl(UuidHelper.toUuidString(UUID.randomUUID()))
			.setResource(FhirBinaryFileHelper.createBinaryFromFile(binaryFile));
	}

}
