package de.yoshteq.diga.secon;

import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class X509CertificateUtil
{

	private static final Pattern IK_PATTERN = Pattern.compile("IK(\\d{9})");

	public static Optional<Integer> extractIkAsInteger(X509Certificate cert)
	{
		return extractIkAsString(cert).map(Integer::valueOf);
	}

	public static Optional<String> extractIkAsString(X509Certificate cert)
	{
		var subjectName = cert.getSubjectX500Principal().getName().toUpperCase();
		return extractIkAsStringFromSubjectName(subjectName);
	}

	public static Optional<Integer> extractIkAsIntegerFromSubjectName(String subjectName)
	{
		return extractIkAsStringFromSubjectName(subjectName).map(Integer::valueOf);
	}
	
	public static Optional<String> extractIkAsStringFromSubjectName(String subjectName)
	{
		Matcher matcher = IK_PATTERN.matcher(subjectName);
		if (matcher.find())
		{
			return Optional.of(matcher.group(1));
		}

		return Optional.empty();
	}

	private X509CertificateUtil()
	{

	}
}
