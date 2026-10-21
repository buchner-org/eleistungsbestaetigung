package de.yoshteq.dcs.sftp;

import java.io.IOException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

@ApplicationScoped
public class SftpClientCreator
{
	@Inject
	@ConfigProperty(name = "dcs.remote.sftp.host")
	String sftpHost;

	@Inject
	@ConfigProperty(name = "dcs.remote.sftp.port")
	int sftpPort;

	@Inject
	@ConfigProperty(name = "dcs.remote.sftp.username")
	String sftpUsername;

	@Inject
	@ConfigProperty(name = "dcs.remote.sftp.key-path")
	String sftpKeyPath;

	public SFTPConnector create() throws IOException
	{
		var sshClient = new SSHClient();
		sshClient.addHostKeyVerifier(new PromiscuousVerifier());
		sshClient.connect(sftpHost, sftpPort);
		sshClient.authPublickey(sftpUsername, sshClient.loadKeys(sftpKeyPath));
		return new SFTPConnector(sshClient);
	}
}
