package org.metafetish.buttplug.components.websocketserver;

import android.content.Context;

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
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.util.BigIntegers;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;

public class CertUtils {
    public static KeyStore getKeystore(Context context) {
        return CertUtils.getKeystore(context, "localhost");
    }

    public static KeyStore getKeystore(Context context, String hostname) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.keystore);
        KeyStore keystore = null;
        try {
            keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(inputStream, null);
        } catch (KeyStoreException | CertificateException | IOException |
                NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return keystore;
//        String filename = "keystore.pfx";
//        File file = new File(context.getFilesDir(), filename);
//        if (file.exists()) {
//            try {
//                KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
////                KeyStore keystore = KeyStore.getInstance("PKCS12");
////                keystore.load(context.openFileInput(filename), null);
//                keystore.load(context.openFileInput(filename), "password".toCharArray());
//                return keystore;
//            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |
// IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return null;
////        KeyStore keystore = null;
////        try {
//////            keystore = KeyStore.getInstance(KeyStore.getDefaultType());
////            keystore = KeyStore.getInstance("PKCS12");
////            keystore.load(null, null);
////            X509Certificate cert = CertUtils.generateSelfSignedCertificate(hostname);
////            keystore.setCertificateEntry("self", cert);
////            FileOutputStream outputStream = context.openFileOutput("keystore.jks", Context
/// .MODE_PRIVATE);
////            keystore.store(outputStream, null);
////        } catch (KeyStoreException | IOException | NoSuchAlgorithmException |
/// CertificateException | OperatorCreationException e) {
////            e.printStackTrace();
////        }
////        return keystore;
    }

    // Note: Much of this code comes from https://stackoverflow.com/a/22247129
    public static X509Certificate generateSelfSignedCertificate(String subject) throws
            NoSuchAlgorithmException, IOException, OperatorCreationException, CertificateException {
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

        SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfoFactory
                .createSubjectPublicKeyInfo(subjectKeyPair.getPublic());
//        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
//        keyPairGenerator.initialize(keyStrength, random);
//        KeyPair subjectKeyPair = keyPairGenerator.generateKeyPair();

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

//        certificateBuilder.addExtension(Extension.subjectAlternativeName, false, new
// GeneralName(GeneralName.dNSName, machineName));
        certificateBuilder.addExtension(Extension.subjectAlternativeName, false, new GeneralName
                (GeneralName.dNSName, "localhost"));
//        certificateBuilder.addExtension(Extension.subjectAlternativeName, false, new
// GeneralName(GeneralName.iPAddress, "127.0.0.1"));
        if (!subject.equals("localhost")) {  // && subject != machineName) {
            certificateBuilder.addExtension(Extension.subjectAlternativeName, false, new
                    GeneralName(GeneralName.dNSName, subject));
        }
        // Issuer
        certificateBuilder.addExtension(Extension.subjectKeyIdentifier, false, new
                JcaX509ExtensionUtils().createSubjectKeyIdentifier(subjectPublicKeyInfo));

        // Add basic constraint
        certificateBuilder.addExtension(Extension.basicConstraints, false, new BasicConstraints
                (true));

        certificateBuilder.addExtension(Extension.extendedKeyUsage, false, new ExtendedKeyUsage
                (new KeyPurposeId[]{KeyPurposeId.id_kp_clientAuth, KeyPurposeId.id_kp_serverAuth}));

        // Signature Algorithm
        final String signatureAlgorithm = "SHA256WithRSA";
        AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find
                (signatureAlgorithm);
        AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
        ContentSigner contentSigner = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(
                (AsymmetricKeyParameter) subjectKeyPair.getPrivate());

        // selfsign certificate
        X509CertificateHolder certificateHolder = certificateBuilder.build(contentSigner);
        X509Certificate certificate = new JcaX509CertificateConverter().setProvider(new
                BouncyCastleProvider()).getCertificate(certificateHolder);

        return certificate;
    }
}
