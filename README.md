## mendelson AS2

The mendelson AS2 software is a JAVA based solution that allows you to quickly and easily connect your partners via the EDIINT AS2 standard.

The solution is available in a dual-license model: a community license (open source) for free download and a commercial version.

Technical

    Asyncronous and syncronous MDN
    Key and certificate management
    Partner management
    Digital signatures
    Message encryption
    Secure transport (SSLv3, TLS 1.0, TLS 1.1, TLS 1.2, TLS 1.3)
    Support for SSL client authentication
    System task to auto clear old log entries
    Data compression (AS2 1.1)
    Multiple attachments (AS2 1.2)
    Multinational support: Localized to german, english and french


Integration

    Easy integration to existing systems, using a partner based file system interface
    Integrated scheduler picks up data from directories
    Message post processing (scripting on receipt)
    Pluggable into any servlet container like Tomcat, Jetty, ...



Monitoring

    Web interface for transaction monitoring
    Email event notification



Encryption and signatures

The following encryption algorithms are supported:

    Triple DES
    DES
    RC2-40
    RC2-64
    RC2-128
    RC2-196
    RC4-40
    RC4-56
    RC4-128
    AES-128
    AES-192
    AES-256
    AES-128 (RSAES_OAEP)
    AES-192 (RSAES_OAEP)
    AES-256 (RSAES_OAEP)

The following hash algorithms are supported:

    SHA-1
    MD5
    SHA-224 (SHA-2)
    SHA-256 (SHA-2)
    SHA-384 (SHA-2)
    SHA-512 (SHA-2)
    SHA-1 (RSASSA-PSS)
    SHA-224 (SHA-2, RSASSA-PSS)
    SHA-256 (SHA-2, RSASSA-PSS)
    SHA-384 (SHA-2, RSASSA-PSS)
    SHA-512 (SHA-2, RSASSA-PSS)
    SHA-3-224 (SHA-3)
    SHA-3-256 (SHA-3)
    SHA-3-384 (SHA-3)
    SHA-3-512 (SHA-3)
    SHA-3-224 (SHA-3, RSASSA-PSS)
    SHA-3-256 (SHA-3, RSASSA-PSS)
    SHA-3-384 (SHA-3, RSASSA-PSS)
    SHA-3-512 (SHA-3, RSASSA-PSS)




Accepted certificates and keys

    Trusted certificates, self signed certificates
    SHA-1 signed certficates, SHA-2 signed certificates
    Trusted by any CA
    Elliptic curve keys/certificates, RSA keys/certificates




Architecture

mendelson AS2 could send and receive AS2 messages from and to trading partners via HTTP and HTTPS. It supports synchronous MDN and asynchronous MDN.

There runs an additional poll thread for every partner that polls special directories per partner and sends matching files to the mendelson AS2 server.

Please have a look at the following diagram for an overview of the inluded components of the mendelson AS2 package. All these components install out-of-the-box if you are using the installer.

The main difference in the architecture between the commercial version and the community version (open source) is that in the commercial version the user interface and the AS2 server are running
in different processes and could even run on different machines/operation systems while the server could run as service.
The community version acts as a desktop system, user interface and server are running in the same process.

AS2 Server:
The server is the core component. It is responsible for the transaction processing and cares for encryption, digital signatures and the communication to all the other components.

AS2 Client (Rich client)
The AS2 client contains the transaction management, partner management, certificate management (commercial version only). It allows to set all server properties and configure the system.

AS2 Client (Web client)
This is an optional component, it allows to monitor the AS2 transactions via a browser.

Database
The database server stores all information about the transactions and the master data of partners, subjects etc.

HTTP Server:
The HTTP server acts as a servlet container for the message receipt servlet. It host alos some information pages. The servlet sends received messages via RMI to the server. There is a HTTP server included in the installation package but its also possible to deploy the AS2 receiver in any other servlet container.

AS2 Sender:
This component sends messages and MDN (async) to the trading partner. It also received MDN if the MDN of an outbound transmission is set to sync.

Notification:
Allows the notification via mail if there occured any event that requires user interaction. 