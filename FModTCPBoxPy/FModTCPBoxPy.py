from socket import *

class FModTCPBoxPy:
    REG = {
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
        "AD0VALUE": (0x30, 4), 
        "AD1VALUE": (0x31, 4),
        "AD2VALUE": (0x32, 4),
        "AD3VALUE": (0x33, 4),
        "AD4VALUE": (0x34, 4),
        "AD5VALUE": (0x35, 4),
        "AD6VALUE": (0x36, 4),
        "AD7VALUE": (0x37, 4),
        "AD8VALUE": (0x38, 4),
        "AD9VALUE": (0x39, 4),
        "ADAVALUE": (0x3A, 4),
        "ADBVALUE": (0x3B, 4),
        "ADCVALUE": (0x3C, 4),
        "ADDVALUE": (0x3D, 4),
        "ADEVALUE": (0x3E, 4),
        "ADFVALUE": (0x3F, 4)
    }

    def __init__(self, IP="169.254.5.5"):
        self.serverIP = IP 
        self.serverPort = 8010          # I/O TCP port
        self.serverUartPort = 8000      # RS232
        self.pktID = 0x0 

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
    # regs = [ reg1_name, reg2_name, ...]
    def readReg(self, regs):
        numreg = len(regs)
        id = self.pktID
        self.pktID = id + 1
        pkt = [0, 0x21, ((id&0xff00)>>8), (id&0x00ff), numreg>>8, numreg&0xff] 
        self.expectedRecvBytes = 8
        for a in regs:
            pkt = pkt + [self.REG[a.upper()][0]]
            self.expectedRecvBytes = self.expectedRecvBytes + self.REG[a.upper()][1] + 1
        cksum = self.calcChecksum(pkt)
        self.sock.send(bytearray(pkt+cksum))
        # Read Reg ans [0x00, 0x23, ID_hi, ID_lo, Num_Byte_hi, Num_Byte_lo, Reg_Addr, Reg_Val, ..., chksum_hi, chksum_lo]
        ans = self.sock.recv(self.expectedRecvBytes)
        return ans 

    # [0x00, 0x22, ID_hi, ID_lo, Num_Bytes_hi, Num_Bytes_lo, Reg_Addr, Reg_val ... chksum_hi, chksum_lo]
    # reg = [ [reg_name, reg_val1, reg_val2 ..], ...]
    def writeReg(self, regs):
        id = self.pktID
        self.pktID = id + 1
        pkt = [0, 0x22, (id&0xff00)>>8, id&0xff, 0, 0]
        num_bytes = 0
        for r in regs:
            pkt = pkt + [ self.REG[r[0].upper()][0] ] + r[1:]
            num_bytes = num_bytes + len(r)
        pkt[4] = (num_bytes&0xff00) >> 8
        pkt[5] = num_bytes&0xff
        cksum = self.calcChecksum(pkt)
        self.sock.send(bytearray(pkt+cksum))
        # Write Reg ans [0x00, 0x24, ID_hi, ID_lo, 0x00, 0x00, chksum_hi, chksum_lo]
        ans = self.sock.recv(8)
        return ans 

    def connect(self):
        self.sock = socket(AF_INET, SOCK_STREAM)
        self.sock.connect((self.serverIP, self.serverPort))

    def disconnect(self):
        self.sock.close()

if __name__ == '__main__':

    fmodbox = FModTCPBoxPy()
    fmodbox.connect()
    
    pkt = fmodbox.readReg(["type", "version", "mac", "ip", "name", "voltage", "outputs"])
    print "recv ", [hex(ord(x)) for x in pkt]
    
    pkt = fmodbox.writeReg([["outputs", 0x0F, 0xF0]])
    print "recv ", [hex(ord(x)) for x in pkt]
    
    pkt = fmodbox.readReg(["outputs"])
    print "recv ", [hex(ord(x)) for x in pkt]
    
    fmodbox.disconnect()
