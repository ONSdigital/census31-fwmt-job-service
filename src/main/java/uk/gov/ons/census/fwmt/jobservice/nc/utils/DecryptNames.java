package uk.gov.ons.census.fwmt.jobservice.nc.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Iterator;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPSecretKeyRing;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;
import org.bouncycastle.util.io.Streams;

import uk.gov.ons.census.fwmt.common.error.GatewayException;

public class DecryptNames {
  
  private static Object lockKey = new Object();

  public static String decryptFile(byte[] secretKeyFile, String householder, char[] passwd) throws GatewayException {
    synchronized (lockKey) {
      PGPPrivateKey secretKey = null;
      PGPPublicKeyEncryptedData encryptedData = null;
      Iterator<PGPPublicKeyEncryptedData> encryptedObjects;
      long encryptedFileKeyId = 0;
      try {
        encryptedObjects = getEncryptedObjects(Base64.getDecoder().decode(householder));
      } catch (IOException e) {
        throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, "Failed to obtain decryption data");
      }
      while (encryptedObjects.hasNext() && secretKey == null) {
        encryptedData = encryptedObjects.next();
        encryptedFileKeyId = encryptedData.getKeyID();

        try {
          secretKey = getSecretKey(secretKeyFile, passwd, encryptedFileKeyId);
        } catch (IOException | PGPException e) {
          throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, e, "Failed to obtain secret key");
        }
      }
      try {
        InputStream decryptedData = null;
        if (encryptedData != null) {
          decryptedData = encryptedData.getDataStream(
              new JcePublicKeyDataDecryptorFactoryBuilder().setProvider(new BouncyCastleProvider()).build(secretKey));
        }
        PGPLiteralData pgpLiteralData = asLiteral(decryptedData);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (pgpLiteralData != null) {
          Streams.pipeAll(pgpLiteralData.getInputStream(), out);
        }

        return out.toString(Charset.defaultCharset());
      } catch (IOException | PGPException e) {
        throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, e, "Failed to stream decryption data");
      }
    }
  }

  private static Iterator<PGPPublicKeyEncryptedData> getEncryptedObjects(final byte[] message) throws IOException {
    try {
      final PGPObjectFactory factory = new PGPObjectFactory(PGPUtil.getDecoderStream(new ByteArrayInputStream(message)),
          new JcaKeyFingerprintCalculator());
      final Object first = factory.nextObject();
      final Object list = (first instanceof PGPEncryptedDataList) ? first : factory.nextObject();
      return ((PGPEncryptedDataList) list).getEncryptedDataObjects();
    } catch (IOException e) {
      throw new IOException(e);
    }
  }

  private static PGPPrivateKey getSecretKey(byte[] pgpSecretKey, char[] password, long encryptedFileKeyId)
      throws IOException, PGPException {
    ByteArrayInputStream bais = new ByteArrayInputStream(pgpSecretKey);
    InputStream decoderStream = PGPUtil.getDecoderStream(bais);
    JcaPGPSecretKeyRing pgpSecretKeys = new JcaPGPSecretKeyRing(decoderStream);
    decoderStream.close();
    Iterator<PGPSecretKey> secretKeys = pgpSecretKeys.getSecretKeys();
    PGPPrivateKey key = null;
    while (key == null && secretKeys.hasNext()) {
      PGPSecretKey k = secretKeys.next();
      long publicKeyId = k.getKeyID();
      if (encryptedFileKeyId == publicKeyId && !k.isPrivateKeyEmpty()) {
        key = k.extractPrivateKey(
            new JcePBESecretKeyDecryptorBuilder().setProvider(new BouncyCastleProvider()).build(password));
      }

    }
    return key;
  }

  private static PGPLiteralData asLiteral(final InputStream clear) throws IOException, PGPException {
    final PGPObjectFactory plainFact = new PGPObjectFactory(clear, new JcaKeyFingerprintCalculator());
    final Object message = plainFact.nextObject();
    if (message instanceof PGPCompressedData) {
      final PGPCompressedData cData = (PGPCompressedData) message;
      final PGPObjectFactory pgpFact = new PGPObjectFactory(cData.getDataStream(), new JcaKeyFingerprintCalculator());
      // Find the first PGPLiteralData object
      Object object = null;
      for (int safety = 0; (safety++ < 1000) && !(object instanceof PGPLiteralData); object = pgpFact.nextObject()) {
        // ignore
      }
      return (PGPLiteralData) object;
    } else if (message instanceof PGPLiteralData) {
      return (PGPLiteralData) message;
    } else {
      throw new PGPException(
          "message is not a simple encrypted file - type unknown: " + message.getClass().getName());
    }
  }
  

  
}
