from socket import *

class FModTCPBoxPy:
    def __init__(self, IP="169.254.5.5"):
        self.serverIP = IP 
        self.serverPort = 8010          # I/O TCP port
        self.serverUartPort = 8000      # RS232
        self.pktID = 0x0 
        self.REG = {
            # Bank 0 : General Info 
            "TYPE": (0x00, 4),
            "VERSION": (0x01, 4), 
            "RESETPROC": (0x02, 0),
            "SAVEUSERPARAMETERS": (0x03, 0) ,
            "RESTOREUSERPARAMETERS": (0x04, 0),
            "RESTOREFACTORYPARAMETERS": (0x05, 0) ,
            "SAVEFACTORYPARAMETERS": (0x06, 0) ,
            "VOLTAGE": (0x07, 4) ,
            "WARNING": (0x08, 4) ,

            # Bank 1": Communication
            "COMOPTIONS": (0x10, 4) ,
            "MAC": (0x11, 6) ,
            "IP":  (0x12, 4) ,
            "SUBNETMASK": (0x13, 4) ,
            "TCPWATCHDOG": (0x14, 1) ,
            "NAME": (0x15, 16) ,
            "UARTCONFIG": (0x16, 1) ,
            "I2CSPDCONFIG": (0x18, 1) ,
            "NUMBEROFUSERS": (0x1A, 1) ,

            # Bank 2": External ports
            "INANALOGTHRESHOLD": (0x20, 4) ,
            "OUTPUTS": (0x21, 2) ,
            "INPUTS":  (0x23, 2) ,

            # Bank 3": AD0-15 
            "AD0VALUE": (0x30, 4) }
        self.expectedRecvBytes = 0

    def calcChecksum(self, ByteStream):
        checksum = 0 
        mask = 0xff00
        for b in ByteStream:
            temp = ((b<<8) & mask) + (b & mask)
            checksum = checksum + ((~temp) & 0xFFFF)
            mask = 0xFFFF - mask
        checksum = ((checksum & 0xFFFF0000)>>16) + (checksum & 0xFFFF)
        checksum = ((checksum & 0xFFFF0000)>>16) + (checksum & 0xFFFF) 
        return [checksum>>8, checksum&0x00FF]

    # Read Reg [0x00, 0x21, ID_hi, ID_lo, Num_Reg_hi, Num_Reg_lo, Reg_Addr, ..., chksum_hi, chksum_lo]
    def readReg(self, addr):
        numreg = len(addr)
        id = self.pktID
        self.pktID = id + 1
        pkt = [0, 0x21, ((id&0xff00)>>8), (id&0x00ff), numreg>>8, numreg&0xff] 
        self.expectedRecvBytes = 8
        for a in addr:
            pkt = pkt + [self.REG[a][0]]
            self.expectedRecvBytes = self.expectedRecvBytes + self.REG[a][1] + 1
        cksum = self.calcChecksum(pkt)
        self.sock.send(bytearray(pkt+cksum))
        return pkt+cksum

    # [0x00, 0x22, ID_hi, ID_lo, Num_Bytes_hi, Num_Bytes_lo, Reg_Addr, Reg_val ... chksum_hi, chksum_lo]
    def writeReg(self, reg):
        id = self.pktID
        self.pktID = id + 1
        pkt = [0, 0x22, (id&0xff00)>>8, id&0xff, 0, 0]
        num_bytes = 0
        for r in reg:
            pkt = pkt + [ self.REG[r[0]][0] ] + r[1:]
            num_bytes = num_bytes + len(r)
        pkt[4] = (num_bytes&0xff00) >> 8
        pkt[5] = num_bytes&0xff
        cksum = self.calcChecksum(pkt)
        self.sock.send(bytearray(pkt+cksum))
        return pkt+cksum

    # Read Reg ans [0x00, 0x23, ID_hi, ID_lo, Num_Byte_hi, Num_Byte_lo, Reg_Addr, Reg_Val, ..., chksum_hi, chksum_lo]
    def getReadAns(self):
        return self.sock.recv(self.expectedRecvBytes)

    # Write Reg ans [0x00, 0x24, ID_hi, ID_lo, 0x00, 0x00, chksum_hi, chksum_lo]
    def getWriteAns(self):
        return self.sock.recv(8)

    def connect(self):
        self.sock = socket(AF_INET, SOCK_STREAM)
        self.sock.connect((self.serverIP, self.serverPort))

    def disconnect(self):
        self.sock.close()

if __name__ == '__main__':

    fmodbox = FModTCPBoxPy()
    fmodbox.connect()
    
    pkt = fmodbox.readReg(["OUTPUTS"])
    print "send ", pkt
    
    data = fmodbox.getReadAns()
    print "recv ", [hex(ord(x)) for x in data]
    
    pkt = fmodbox.writeReg([["OUTPUTS", 0x0F, 0xF0]])
    print "send ", pkt
    
    data = fmodbox.getWriteAns()
    print "recv ", [hex(ord(x)) for x in data]
    
    pkt = fmodbox.readReg(["OUTPUTS"])
    print "send ", pkt
    
    data = fmodbox.getReadAns()
    print "recv ", [hex(ord(x)) for x in data]
    
    fmodbox.disconnect()
