package com.arcao.geocaching4locus.data.api.internal.okhttp

import android.content.Context
import android.os.Build
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import timber.log.Timber
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.net.UnknownHostException
import javax.net.ssl.SSLContext

import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * Enables TLS v1.1 and v1.2 when creating SSLSockets.
 *
 *
 * For some reason, android supports TLS v1.1 and v1.2 from API 16, but enables it by
 * default only from API 20.
 * @link https://developer.android.com/reference/javax/net/ssl/SSLSocket.html
 * @see SSLSocketFactory
 */
class ModernTlsSocketFactory internal constructor(private val delegate: SSLSocketFactory) : SSLSocketFactory() {

    override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites

    override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites

    @Throws(IOException::class)
    override fun createSocket(s: Socket, host: String, port: Int, autoClose: Boolean): Socket {
        return patch(delegate.createSocket(s, host, port, autoClose))
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int): Socket {
        return patch(delegate.createSocket(host, port))
    }

    @Throws(IOException::class, UnknownHostException::class)
    override fun createSocket(host: String, port: Int, localHost: InetAddress, localPort: Int): Socket {
        return patch(delegate.createSocket(host, port, localHost, localPort))
    }

    @Throws(IOException::class)
    override fun createSocket(host: InetAddress, port: Int): Socket {
        return patch(delegate.createSocket(host, port))
    }

    @Throws(IOException::class)
    override fun createSocket(address: InetAddress, port: Int, localAddress: InetAddress, localPort: Int): Socket {
        return patch(delegate.createSocket(address, port, localAddress, localPort))
    }

    private fun patch(s: Socket): Socket {
        if (s is SSLSocket) {
            s.enabledProtocols = MODERN_TLS_ONLY
        }
        return s
    }

    companion object {
        private val MODERN_TLS_ONLY = arrayOf("TLSv1.1", "TLSv1.2")
    }
}

fun OkHttpClient.Builder.enableTls12(context: Context?): OkHttpClient.Builder {
    if (Build.VERSION.SDK_INT in 16..21) {
        context?.patchSecurityApis()

        try {
            val sc = SSLContext.getInstance("TLS")
            sc.init(null, null, null)

            @Suppress("DEPRECATION")
            sslSocketFactory(ModernTlsSocketFactory(sc.socketFactory))

            connectionSpecs(
                listOf(
                    ConnectionSpec.MODERN_TLS,
                    ConnectionSpec.COMPATIBLE_TLS,
                    ConnectionSpec.CLEARTEXT
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Error while setting TLS 1.2")
        }
    }

    return this
}

private fun Context.patchSecurityApis() {
    try {
        // https://developer.android.com/training/articles/security-gms-provider.html
        // this can take anywhere from 30-50 milliseconds (on more recent devices) to 350 ms (on older devices)
        //
        // Once the Provider is updated, all calls to security APIs (including SSL APIs) are routed through it.
        // (However, this does not apply to android.net.SSLCertificateSocketFactory,
        // which remains vulnerable to such exploits as CVE-2014-0224.)

        ProviderInstaller.installIfNeeded(this)
    } catch (e: GooglePlayServicesRepairableException) {
        // Indicates that Google Play services is out of date, disabled, etc.
        Timber.e(e)

        // Prompt the user to install/update/enable Google Play services.
        GoogleApiAvailability.getInstance()
            .showErrorNotification(this, e.connectionStatusCode)
    } catch (e: Exception) {
        // Indicates a non-recoverable error; the ProviderInstaller is not able
        // to install an up-to-date Provider.

        Timber.e(e)
    }
}