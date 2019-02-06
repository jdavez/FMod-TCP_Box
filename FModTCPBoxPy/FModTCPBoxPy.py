from socket import *

serverIP = "169.254.5.5"   # default address
serverPort = 8010          # I/O TCP port
serverUartPort = 8000      # RS232
uniqueID = 0x4444 

# Bank 0 : General Info 
TCPSERVERTYPE				= 0x00 
TCPSERVERVERSION			= 0x01 
TCPSERVERRESETPROC			= 0x02 
TCPSERVERSAVEUSERPARAMETERS		= 0x03 
TCPSERVERRESTOREUSERPARAMETERS		= 0x04 
TCPSERVERRESTOREFACTORYPARAMETERS	= 0x05 
TCPSERVERSAVEFACTORYPARAMETERS		= 0x06 
TCPSERVERVOLTAGE			= 0x07 
TCPSERVERWARNING			= 0x08 
	
# Bank 1 : Communication
TCPSERVERCOMOPTIONS			= 0x10 
TCPSERVERMAC				= 0x11 
TCPSERVERIP				= 0x12 
TCPSERVERSUBNETMASK			= 0x13 
TCPSERVERTCPWATCHDOG			= 0x14 
TCPSERVERNAME				= 0x15 
TCPSERVERUARTCONFIG			= 0x16 
TCPSERVERI2CSPDCONFIG			= 0x18 
TCPSERVERNUMBEROFUSERS			= 0x1A 

# Bank 2 : External ports
TCPSERVERINANALOGTHRESHOLD		= 0x20 
TCPSERVEROUTPUTS			= 0x21 
TCPSERVERINPUTS				= 0x23 

# Bank 3 : AD
TCPSERVERAD0VALUE			= 0x30 
TCPSERVERAD1VALUE			= 0x31 
TCPSERVERAD2VALUE			= 0x32 
TCPSERVERAD3VALUE			= 0x33 
TCPSERVERAD4VALUE			= 0x34 
TCPSERVERAD5VALUE			= 0x35 
TCPSERVERAD6VALUE			= 0x36 
TCPSERVERAD7VALUE			= 0x37 
TCPSERVERAD8VALUE			= 0x38 
TCPSERVERAD9VALUE			= 0x39 
TCPSERVERADAVALUE			= 0x3A 
TCPSERVERADBVALUE			= 0x3B 
TCPSERVERADCVALUE			= 0x3C 
TCPSERVERADDVALUE			= 0x3D 
TCPSERVERADEVALUE			= 0x3E 
TCPSERVERADFVALUE			= 0x3F 

        
def calcChecksum(ByteStream):
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
def readRegPacket(id, addr):
    numreg = len(addr)
    pkt = [0, 0x21, ((id&0xff00)>>8), (id&0x00ff), numreg>>8, numreg&0xff] + addr 
    cksum = calcChecksum(pkt)
    return pkt+cksum

# [0x00, 0x22, ID_hi, ID_lo, Num_Bytes_hi, Num_Bytes_lo, Reg_Addr, Reg_val ... chksum_hi, chksum_lo]
def writeRegPacket(id, reg):
    pkt = [0, 0x22, (id&0xff00)>>8, id&0xff, 0, 0]
    num_bytes = 0
    for r in reg:
        pkt = pkt + r
        num_bytes = num_bytes + len(r)
    pkt[4] = (num_bytes&0xff00) >> 8
    pkt[5] = num_bytes&0xff
    cksum = calcChecksum(pkt)
    return pkt+cksum
    
sock = socket(AF_INET, SOCK_STREAM)
sock.connect((serverIP, serverPort))

pkt = readRegPacket(uniqueID, [TCPSERVEROUTPUTS])
uniqueID = uniqueID + 1
print "send ", pkt
sock.send(str(bytearray(pkt)))

# Read Reg ans [0x00, 0x23, ID_hi, ID_lo, Num_Byte_hi, Num_Byte_lo, Reg_Addr, Reg_Val, ..., chksum_hi, chksum_lo]
data = sock.recv(11)
print "recv ", [hex(ord(x)) for x in data]

pkt = writeRegPacket(uniqueID, [[TCPSERVEROUTPUTS, 0xFF, 0xFF]])
uniqueID = uniqueID + 1
print "send ", pkt
sock.send(str(bytearray(pkt)))

# Write Reg ans [0x00, 0x24, ID_hi, ID_lo, 0x00, 0x00, chksum_hi, chksum_lo]
data = sock.recv(8)
print "recv ", [hex(ord(x)) for x in data]

pkt = readRegPacket(uniqueID, [TCPSERVEROUTPUTS])
uniqueID = uniqueID + 1
print "send ", pkt
sock.send(str(bytearray(pkt)))

data = sock.recv(11)
print "recv ", [hex(ord(x)) for x in data]

sock.close()
