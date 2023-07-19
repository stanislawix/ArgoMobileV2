package pl.argo.argomobile

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.common.base.Preconditions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import org.ros.address.InetAddressFactory
import org.ros.android.MasterChooser
import org.ros.android.NodeMainExecutorService
import org.ros.android.NodeMainExecutorService.LocalBinder
import org.ros.android.NodeMainExecutorServiceListener
import org.ros.exception.RosRuntimeException
import org.ros.node.NodeMainExecutor
import java.net.NetworkInterface
import java.net.SocketException
import java.net.URI
import java.net.URISyntaxException

// The source code was copied from a decompiled .class file, then extension was changed
abstract class RosActivity protected constructor(
    notificationTicker: String,
    notificationTitle: String,
    customMasterUri: URI? = null
) : AppCompatActivity(), CoroutineScope by MainScope() {
    private val nodeMainExecutorServiceConnection: NodeMainExecutorServiceConnection
    private val notificationTicker: String
    private val notificationTitle: String
    private var masterChooserActivity: Class<*>
    private var masterChooserRequestCode: Int
    protected var nodeMainExecutorService: NodeMainExecutorService? = null

    init {
        masterChooserActivity = MasterChooser::class.java
        masterChooserRequestCode = 0
        this.notificationTicker = notificationTicker
        this.notificationTitle = notificationTitle
        nodeMainExecutorServiceConnection = NodeMainExecutorServiceConnection(customMasterUri)
    }

    protected constructor(
        notificationTicker: String,
        notificationTitle: String,
        activity: Class<*>,
        requestCode: Int
    ) : this(notificationTicker, notificationTitle) {
        masterChooserActivity = activity
        masterChooserRequestCode = requestCode
    }

    override fun onStart() {
        super.onStart()
        bindNodeMainExecutorService()
    }

    protected fun bindNodeMainExecutorService() {
        val intent = Intent(this, NodeMainExecutorService::class.java)
        intent.action = "org.ros.android.ACTION_START_NODE_RUNNER_SERVICE"
        intent.putExtra("org.ros.android.EXTRA_NOTIFICATION_TICKER", notificationTicker)
        intent.putExtra("org.ros.android.EXTRA_NOTIFICATION_TITLE", notificationTitle)
        startService(intent)
        Preconditions.checkState(
            this.bindService(
                intent,
                nodeMainExecutorServiceConnection,
                BIND_AUTO_CREATE
            ), "Failed to bind NodeMainExecutorService."
        )
    }

    override fun onDestroy() {
        unbindService(nodeMainExecutorServiceConnection)
        nodeMainExecutorService!!.removeListener(nodeMainExecutorServiceConnection.serviceListener)
        super.onDestroy()
    }

    protected fun init() {
        val asyncServiceInitialization = async {
            this@RosActivity.init(nodeMainExecutorService)
        }
        asyncServiceInitialization.start()
    }

    protected abstract fun init(var1: NodeMainExecutor?)


    fun startMasterChooser() {
        Preconditions.checkState(masterUri == null)

        val startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intentData = result.data

                    if (intentData != null) {

                        val networkInterfaceName: String? =
                            intentData.getStringExtra("ROS_MASTER_NETWORK_INTERFACE")

                        val host: String =
                            if (!networkInterfaceName.isNullOrBlank()) {
                                try {
                                    val networkInterface =
                                        NetworkInterface.getByName(networkInterfaceName)
                                    InetAddressFactory.newNonLoopbackForNetworkInterface(
                                        networkInterface
                                    ).hostAddress
                                } catch (socketException: SocketException) {
                                    throw RosRuntimeException(socketException)
                                }
                            } else {
                                defaultHostAddress
                            }

                        nodeMainExecutorService?.rosHostname = host

                        if (intentData.getBooleanExtra("ROS_MASTER_CREATE_NEW", false)) {
                            nodeMainExecutorService!!.startMaster(
                                intentData.getBooleanExtra(
                                    "ROS_MASTER_PRIVATE",
                                    true
                                )
                            )
                        } else {
                            val uri: URI
                            uri = try {
                                URI(intentData.getStringExtra("ROS_MASTER_URI"))
                            } catch (uriSyntaxException: URISyntaxException) {
                                throw RosRuntimeException(uriSyntaxException)
                            }
                            nodeMainExecutorService!!.masterUri = uri
                        }

                        val asyncJob = async {
                            this@RosActivity.init(nodeMainExecutorService)
                        }
                        asyncJob.start()
                    } else {
                        nodeMainExecutorService?.rosHostname = defaultHostAddress
//                        nodeMainExecutorService!!.startMaster(true) // niby ros_master_create_new domyslnie jest false to idk czy startowac mastera tu
                    }

                } else {
                    nodeMainExecutorService!!.forceShutdown()
                }
            }

        startForResult.launch(
            Intent(
                this, MasterChooser::class.java
            )
        )
    }

//    fun startMasterChooser() {
//        Preconditions.checkState(masterUri == null)
//
//        val getContent = registerForActivityResult(PickRosMaster) { uri: Uri? ->
//            // Handle the returned Uri
//        }
//
//        getContent.launch(masterChooserRequestCode)
////        super.startActivityForResult(Intent(this, masterChooserActivity), masterChooserRequestCode)
//    }

//    class PickRosMaster : ActivityResultContract<Int, Uri?>() {
//        override fun createIntent(context: Context, masterChooserRequestCode: Int) =
//            Intent(context, MasterChooser::class.java).apply {
//                putExtra("requestCode", masterChooserRequestCode)
//            }
//
//        override fun parseResult(resultCode: Int, result: Intent?) : Uri? {
//            if (resultCode != Activity.RESULT_OK) {
//                return null
//            }
//            return result?.getParcelableExtra("android.intent.extra.ringtone.PICKED_URI")
//        }
//    }

    val masterUri: URI?
        get() {
//            Preconditions.checkNotNull(nodeMainExecutorService)
            return nodeMainExecutorService!!.masterUri
        }

    val rosHostname: String
        get() {
//            Preconditions.checkNotNull(nodeMainExecutorService)
            return nodeMainExecutorService!!.rosHostname
        }

//    @Deprecated("Deprecated in Java")
//    override fun startActivityForResult(intent: Intent, requestCode: Int) {
//        Preconditions.checkArgument(requestCode != masterChooserRequestCode)
//        super.startActivityForResult(intent, requestCode)
//    }

//    @Deprecated("Deprecated in Java")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (onActivityResultCallback != null) {
//            onActivityResultCallback!!.execute(requestCode, resultCode, data)
//        }
//    }

    private val defaultHostAddress: String
        get() = InetAddressFactory.newNonLoopback().hostAddress

    private inner class NodeMainExecutorServiceConnection(private val customMasterUri: URI?) :
        ServiceConnection {
        var serviceListener: NodeMainExecutorServiceListener? = null
            private set

        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            nodeMainExecutorService = (binder as LocalBinder).service
            if (customMasterUri != null) {
                nodeMainExecutorService!!.masterUri = customMasterUri
                nodeMainExecutorService!!.rosHostname = defaultHostAddress
            }
            serviceListener = NodeMainExecutorServiceListener {
                if (!this@RosActivity.isFinishing) {
                    finish()
                }
            }
            nodeMainExecutorService!!.addListener(serviceListener)
            if (masterUri == null) {
                startMasterChooser()
            } else {
                this@RosActivity.init()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            nodeMainExecutorService!!.removeListener(serviceListener)
            serviceListener = null
        }
    }

    companion object {
        protected const val MASTER_CHOOSER_REQUEST_CODE = 0
    }
}