package de.yoshteq.dcs.sftp;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.EnumSet;

import org.apache.commons.io.FilenameUtils;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.Response.StatusCode;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPException;

public class SFTPConnector implements AutoCloseable
{
	private static final int SFTP_FILE_WINDOW_SIZE = 64 * 1024;

	public record SftpFileTransferInformation(Path savingDestination, byte[] file)
	{
	}

	private final SSHClient sshClient;

	public SFTPConnector(SSHClient sshClient) throws IOException
	{
		super();
		this.sshClient = sshClient;
		sftpClient = sshClient.newSFTPClient();
	}

	private final SFTPClient sftpClient;

	public void transferFile(SftpFileTransferInformation fileTransferInformation) throws FileAlreadyExistsException, IOException
	{
		transferFile(fileTransferInformation.savingDestination, fileTransferInformation.file);
	}

	public void transferFile(Path destination, byte[] file) throws IOException
	{
		var filePath = FilenameUtils.separatorsToUnix(destination.toString());
		if (fileExists(filePath))
		{
			throw new FileAlreadyExistsException(filePath);
		}

		var folderPath = FilenameUtils.separatorsToUnix(destination.getParent().toString());
		sftpClient.mkdirs(folderPath);

		try (var remoteFile = sftpClient.open(filePath, EnumSet.of(OpenMode.CREAT, OpenMode.WRITE, OpenMode.EXCL));
			var outputStream = remoteFile.new RemoteFileOutputStream())
		{
			var remaining = file.length;
			var offset = 0;

			while (remaining > 0)
			{
				outputStream.write(file, offset, Math.min(SFTP_FILE_WINDOW_SIZE, remaining));
				remaining -= SFTP_FILE_WINDOW_SIZE;
				offset += SFTP_FILE_WINDOW_SIZE;
			}
		}
	}

	private boolean fileExists(String filePath) throws IOException
	{
		try
		{
			return sftpClient.lstat(filePath) != null;
		}
		catch (SFTPException e)
		{
			if (e.getStatusCode() == StatusCode.NO_SUCH_FILE)
			{
				return false;
			}
			throw e;
		}
	}

	@Override
	public void close() throws IOException
	{
		sftpClient.close();
		sshClient.disconnect();
	}

}
