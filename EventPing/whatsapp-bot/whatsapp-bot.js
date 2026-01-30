const { default: makeWASocket, useMultiFileAuthState, DisconnectReason } = require('@whiskeysockets/baileys');
const express = require('express');
const qrcode = require('qrcode-terminal');
const pino = require('pino');
const app = express();
const port = 3000;

app.use(express.json());

let sock;

async function connectToWhatsApp() {
    const { state, saveCreds } = await useMultiFileAuthState('auth_info_baileys');

    sock = makeWASocket({
        auth: state,
        printQRInTerminal: true,
        logger: pino({ level: 'silent' }),
    });

    sock.ev.on('creds.update', saveCreds);

    sock.ev.on('connection.update', (update) => {
        const { connection, lastDisconnect, qr } = update;
        
        if (qr) {
            console.log("Scan this QR Code to login:");
            qrcode.generate(qr, { small: true });
        }

        if (connection === 'close') {
            const shouldReconnect = (lastDisconnect.error)?.output?.statusCode !== DisconnectReason.loggedOut;
            console.log('Connection closed due to ', lastDisconnect.error, ', reconnecting ', shouldReconnect);
            if (shouldReconnect) {
                connectToWhatsApp();
            }
        } else if (connection === 'open') {
            console.log('✅ WhatsApp connection opened successfully!');
        }
    });
}

// REST API to send reminders
app.post('/send-reminder', async (req, res) => {
    try {
        const { phoneNumber, eventTitle, eventDateTime, description } = req.body;

        if (!phoneNumber) {
            return res.status(400).json({ success: false, message: 'Phone number is required' });
        }

        // Format phone number to JID
        let jid = phoneNumber.replace(/\+/g, '').replace(/\s/g, '');
        if (!jid.includes('@s.whatsapp.net')) {
            jid = `${jid}@s.whatsapp.net`;
        }

        // Send message
        const message = `📅 *Event Reminder*\n\nEvent: *${eventTitle}*\nDate: ${eventDateTime}\n${description}\n\nSee you there!`;
        
        await sock.sendMessage(jid, { text: message });
        console.log(`Sent reminder to ${jid} for event ${eventTitle}`);

        res.json({ success: true, message: 'Reminder sent' });

    } catch (error) {
        console.error('Failed to send reminder:', error);
        res.status(500).json({ success: false, message: error.message });
    }
});

// Health check and connection status
app.get('/health', (req, res) => {
    res.json({
        status: 'running',
        whatsappConnected: sock?.user ? true : false
    });
});

app.listen(port, () => {
    console.log(`WhatsApp Bot listening on port ${port}`);
    connectToWhatsApp();
});
