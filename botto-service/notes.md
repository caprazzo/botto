### Types of bot output:
* one packet
* an iterable of packets
* something that allows to program timed release of packets

### Annotations:
    @Receive(in = {Packet | Message | Presence | Command})
    @Reply(in = { Packet | Message | Presence | Command }, out = [ ... ])
    @Sender

### General Structure:
   - AbstractBotService: implement this to embed the service
        - implement initialize to configure the service
        - implement run to add subdomains and bots

#### Types of bots:

* Receive bot: simply receives packets and does nothing
    * configured using @Packet / @Message / ...
    * configured using @Command(class)
    * maybe @Receive(Packet | Message | Presence | Command)

* Reply bot: responds to an event (packet or subtype of packet) with
    * echo bot
    * echo bot with multiple responses
    * weather bot
    * stopwatch bot, alarm bot
    * ad-hoc command reply bot
    * configured using method-based @Reply

* Action bot: can send packets on its own, to any recipient
    * same response types of the Reply bot
    * can specify recipient (? infrastructure-controlled)
    * can specify sender (? implementation specific - requires plugin)
    * integration bot: interaction initiated not necessarily from XMPP
    * ad-hoc command action bot
    * obtain botService instance using @BotService on a setter

* Questions:
- how to negotiate roster subscription ? (infrastructure, configuration-based "auto-add-to-reoster")
-

### Other Ideas

Use this to build an "external plugin" infrastructure