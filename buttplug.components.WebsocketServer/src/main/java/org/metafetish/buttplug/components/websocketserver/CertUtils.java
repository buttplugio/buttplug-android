package org.metafetish.buttplug.components.websocketserver;

import android.content.Context;
import android.support.v4.util.Pair;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.util.BigIntegers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class CertUtils {
    public static KeyStore getKeystore(Context context) {
        return CertUtils.getKeystore(context, null);
    }

    public static KeyStore getKeystore(Context context, Map<String, String> secureHostPairs) {

        KeyStore keyStore = null;

        String filename = "keystore.bks";
        File file = new File(context.getFilesDir(), filename);
        boolean save = false;
        if (file.exists()) {
            try {
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(context.openFileInput(filename), null);
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |
                    IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null, null);
            } catch (CertificateException | NoSuchAlgorithmException | IOException |
                    KeyStoreException e) {
                e.printStackTrace();
            }
            save = true;
        }
        if (keyStore != null) {
            try {
                for (String ipAddress : secureHostPairs.keySet()) {
                    String hostname = secureHostPairs.get(ipAddress);
                    if (!keyStore.containsAlias(hostname)) {
                        Pair<PrivateKey, X509Certificate> pair = CertUtils
                                .generateSelfSignedCertificate(hostname, ipAddress);
                        keyStore.setKeyEntry(hostname, pair.first, null, new Certificate[]{pair
                                .second});
                        save = true;
                    }
                }
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException |
                    CertificateException | OperatorCreationException | UnrecoverableKeyException
                    | InvalidKeySpecException e) {
                e.printStackTrace();
            }
            if (save) {
                try {
                    FileOutputStream outputStream = context.openFileOutput("keystore.bks",
                            Context.MODE_PRIVATE);
                    keyStore.store(outputStream, null);
                } catch (IOException | CertificateException | NoSuchAlgorithmException |
                        KeyStoreException e) {
                    e.printStackTrace();
                }
            }
        }
        return keyStore;
    }

    // Note: Much of this code comes from https://stackoverflow.com/a/22247129
    public static Pair<PrivateKey, X509Certificate> generateSelfSignedCertificate(String subject,
                                                                                  String ipAddress)
            throws
            NoSuchAlgorithmException, IOException, OperatorCreationException,
            CertificateException, KeyStoreException, UnrecoverableKeyException,
            InvalidKeySpecException {
        Security.addProvider(new BouncyCastleProvider());

        final int keyStrength = 2048;

        // Generating Random Numbers
        SecureRandom random = new SecureRandom();
        BigInteger publicExponent = BigInteger.valueOf(0x10001);

        // Serial Number
        BigInteger serial = BigIntegers.createRandomInRange(BigInteger.ONE, BigInteger.valueOf
                (Long.MAX_VALUE), random);

        // Subject Public Key
        RSAKeyPairGenerator keyPairGenerator = new RSAKeyPairGenerator();
        RSAKeyGenerationParameters keyGenerationParameters = new RSAKeyGenerationParameters
                (publicExponent, random, keyStrength, 80);
        keyPairGenerator.init(keyGenerationParameters);
        AsymmetricCipherKeyPair subjectKeyPair = keyPairGenerator.generateKeyPair();
        AsymmetricKeyParameter privateKeyParameter = subjectKeyPair.getPrivate();
        PrivateKeyInfo privateKeyInfo = PrivateKeyInfoFactory.createPrivateKeyInfo
                (privateKeyParameter);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyInfo
                .parsePrivateKey().toASN1Primitive().getEncoded());
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

        SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfoFactory
                .createSubjectPublicKeyInfo(subjectKeyPair.getPublic());

        // Issuer
        X500Name issuerName = new X500Name("CN=" + subject);
        // Subject DN
        X500Name subjectName = new X500Name("CN=" + subject);

        // Valid For
        Calendar notBefore = Calendar.getInstance();
        Calendar notAfter = Calendar.getInstance();
        notAfter.add(Calendar.YEAR, 2);

        // The Certificate Generator
        X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(issuerName,
                serial, notBefore.getTime(), notAfter.getTime(), subjectName, subjectPublicKeyInfo);

        List<ASN1Encodable> sanList = new ArrayList<>();
        sanList.add(new GeneralName(GeneralName.dNSName, subject));
        sanList.add(new GeneralName(GeneralName.iPAddress, ipAddress));
        if (!subject.equals("localhost")) {
            sanList.add(new GeneralName(GeneralName.dNSName, "localhost"));
        }
        if (!ipAddress.equals("127.0.0.1")) {
            sanList.add(new GeneralName(GeneralName.iPAddress, "127.0.0.1"));
        }
        ASN1Encodable[] sanArray = sanList.toArray(new ASN1Encodable[sanList.size()]);
        certificateBuilder.addExtension(Extension.subjectAlternativeName, false, new DERSequence
                (sanArray));

        // Issuer
        certificateBuilder.addExtension(Extension.subjectKeyIdentifier, false, new
                JcaX509ExtensionUtils().createSubjectKeyIdentifier(subjectPublicKeyInfo));

        // Add basic constraint
        certificateBuilder.addExtension(Extension.basicConstraints, false, new BasicConstraints
                (false));

        certificateBuilder.addExtension(Extension.extendedKeyUsage, false, new ExtendedKeyUsage
                (new KeyPurposeId[]{KeyPurposeId.id_kp_clientAuth, KeyPurposeId.id_kp_serverAuth}));

        // Signature Algorithm
        final String signatureAlgorithm = "SHA256WithRSA";
        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find
                (signatureAlgorithm);
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
        ContentSigner contentSigner = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build
                (privateKeyParameter);

        // selfsign certificate
        X509CertificateHolder certificateHolder = certificateBuilder.build(contentSigner);
        X509Certificate certificate = new JcaX509CertificateConverter().setProvider(new
                BouncyCastleProvider()).getCertificate(certificateHolder);

        return new Pair<PrivateKey, X509Certificate>(privateKey, certificate);
    }
}
