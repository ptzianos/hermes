package org.hermes.hd

import org.hermes.utils.FrozenHashMap

object SLIP44 {
    data class Entry(val index: Int, val hex: String, val symbol: String) {
        val walletIndex: Long = index + BIP32.HARDENED_KEY_OFFSET
    }

    val entries = arrayOf(
        Entry(0,"0x80000000","BTC"),
        Entry(2,"0x80000002","LTC"),
        Entry(3,"0x80000003","DOGE"),
        Entry(4,"0x80000004","RDD"),
        Entry(5,"0x80000005","DASH"),
        Entry(6,"0x80000006","PPC"),
        Entry(7,"0x80000007","NMC"),
        Entry(8,"0x80000008","FTC"),
        Entry(9,"0x80000009","XCP"),
        Entry(10,"0x8000000a","BLK"),
        Entry(11,"0x8000000b","NSR"),
        Entry(12,"0x8000000c","NBT"),
        Entry(13,"0x8000000d","MZC"),
        Entry(14,"0x8000000e","VIA"),
        Entry(15,"0x8000000f","XCH"),
        Entry(16,"0x80000010","RBY"),
        Entry(17,"0x80000011","GRS"),
        Entry(18,"0x80000012","DGC"),
        Entry(19,"0x80000013","CCN"),
        Entry(20,"0x80000014","DGB"),
        Entry(22,"0x80000016","MONA"),
        Entry(23,"0x80000017","CLAM"),
        Entry(24,"0x80000018","XPM"),
        Entry(25,"0x80000019","NEOS"),
        Entry(26,"0x8000001a","JBS"),
        Entry(27,"0x8000001b","ZRC"),
        Entry(28,"0x8000001c","VTC"),
        Entry(29,"0x8000001d","NXT"),
        Entry(30,"0x8000001e","BURST"),
        Entry(31,"0x8000001f","MUE"),
        Entry(32,"0x80000020","ZOOM"),
        Entry(33,"0x80000021","VASH"),
        Entry(34,"0x80000022","CDN"),
        Entry(35,"0x80000023","SDC"),
        Entry(36,"0x80000024","PKB"),
        Entry(37,"0x80000025","PND"),
        Entry(38,"0x80000026","START"),
        Entry(39,"0x80000027","MOIN"),
        Entry(40,"0x80000028","EXP"),
        Entry(41,"0x80000029","EMC2"),
        Entry(42,"0x8000002a","DCR"),
        Entry(43,"0x8000002b","XEM"),
        Entry(44,"0x8000002c","PART"),
        Entry(45,"0x8000002d","ARG"),
        Entry(48,"0x80000030","SHR"),
        Entry(49,"0x80000031","GCR"),
        Entry(50,"0x80000032","NVC"),
        Entry(51,"0x80000033","AC"),
        Entry(52,"0x80000034","BTCD"),
        Entry(53,"0x80000035","DOPE"),
        Entry(54,"0x80000036","TPC"),
        Entry(55,"0x80000037","AIB"),
        Entry(56,"0x80000038","EDRC"),
        Entry(57,"0x80000039","SYS"),
        Entry(58,"0x8000003a","SLR"),
        Entry(59,"0x8000003b","SMLY"),
        Entry(60,"0x8000003c","ETH"),
        Entry(61,"0x8000003d","ETC"),
        Entry(62,"0x8000003e","PSB"),
        Entry(63,"0x8000003f","LDCN"),
        Entry(65,"0x80000041","XBC"),
        Entry(66,"0x80000042","IOP"),
        Entry(67,"0x80000043","NXS"),
        Entry(68,"0x80000044","INSN"),
        Entry(69,"0x80000045","OK"),
        Entry(70,"0x80000046","BRIT"),
        Entry(71,"0x80000047","CMP"),
        Entry(72,"0x80000048","CRW"),
        Entry(73,"0x80000049","BELA"),
        Entry(74,"0x8000004a","ICX"),
        Entry(75,"0x8000004b","FJC"),
        Entry(76,"0x8000004c","MIX"),
        Entry(77,"0x8000004d","XVG"),
        Entry(78,"0x8000004e","EFL"),
        Entry(79,"0x8000004f","CLUB"),
        Entry(80,"0x80000050","RICHX"),
        Entry(81,"0x80000051","POT"),
        Entry(82,"0x80000052","QRK"),
        Entry(83,"0x80000053","TRC"),
        Entry(84,"0x80000054","GRC"),
        Entry(85,"0x80000055","AUR"),
        Entry(86,"0x80000056","IXC"),
        Entry(87,"0x80000057","NLG"),
        Entry(88,"0x80000058","BITB"),
        Entry(89,"0x80000059","BTA"),
        Entry(90,"0x8000005a","XMY"),
        Entry(91,"0x8000005b","BSD"),
        Entry(92,"0x8000005c","UNO"),
        Entry(93,"0x8000005d","MTR"),
        Entry(94,"0x8000005e","GB"),
        Entry(95,"0x8000005f","SHM"),
        Entry(96,"0x80000060","CRX"),
        Entry(97,"0x80000061","BIQ"),
        Entry(98,"0x80000062","EVO"),
        Entry(99,"0x80000063","STO"),
        Entry(100,"0x80000064","BIGUP"),
        Entry(101,"0x80000065","GAME"),
        Entry(102,"0x80000066","DLC"),
        Entry(103,"0x80000067","ZYD"),
        Entry(104,"0x80000068","DBIC"),
        Entry(105,"0x80000069","STRAT"),
        Entry(106,"0x8000006a","SH"),
        Entry(107,"0x8000006b","MARS"),
        Entry(108,"0x8000006c","UBQ"),
        Entry(109,"0x8000006d","PTC"),
        Entry(110,"0x8000006e","NRO"),
        Entry(111,"0x8000006f","ARK"),
        Entry(112,"0x80000070","USC"),
        Entry(113,"0x80000071","THC"),
        Entry(114,"0x80000072","LINX"),
        Entry(115,"0x80000073","ECN"),
        Entry(116,"0x80000074","DNR"),
        Entry(117,"0x80000075","PINK"),
        Entry(118,"0x80000076","ATOM"),
        Entry(119,"0x80000077","PIVX"),
        Entry(120,"0x80000078","FLASH"),
        Entry(121,"0x80000079","ZEN"),
        Entry(122,"0x8000007a","PUT"),
        Entry(123,"0x8000007b","ZNY"),
        Entry(124,"0x8000007c","UNIFY"),
        Entry(125,"0x8000007d","XST"),
        Entry(126,"0x8000007e","BRK"),
        Entry(127,"0x8000007f","VC"),
        Entry(128,"0x80000080","XMR"),
        Entry(129,"0x80000081","VOX"),
        Entry(130,"0x80000082","NAV"),
        Entry(131,"0x80000083","FCT"),
        Entry(132,"0x80000084","EC"),
        Entry(133,"0x80000085","ZEC"),
        Entry(134,"0x80000086","LSK"),
        Entry(135,"0x80000087","STEEM"),
        Entry(136,"0x80000088","XZC"),
        Entry(137,"0x80000089","RBTC"),
        Entry(139,"0x8000008b","RPT"),
        Entry(140,"0x8000008c","LBC"),
        Entry(141,"0x8000008d","KMD"),
        Entry(142,"0x8000008e","BSQ"),
        Entry(143,"0x8000008f","RIC"),
        Entry(144,"0x80000090","XRP"),
        Entry(145,"0x80000091","BCH"),
        Entry(146,"0x80000092","NEBL"),
        Entry(147,"0x80000093","ZCL"),
        Entry(148,"0x80000094","XLM"),
        Entry(149,"0x80000095","NLC2"),
        Entry(150,"0x80000096","WHL"),
        Entry(151,"0x80000097","ERC"),
        Entry(152,"0x80000098","DMD"),
        Entry(153,"0x80000099","BTM"),
        Entry(154,"0x8000009a","BIO"),
        Entry(155,"0x8000009b","XWC"),
        Entry(156,"0x8000009c","BTG"),
        Entry(157,"0x8000009d","BTC2X"),
        Entry(158,"0x8000009e","SSN"),
        Entry(159,"0x8000009f","TOA"),
        Entry(160,"0x800000a0","BTX"),
        Entry(161,"0x800000a1","ACC"),
        Entry(162,"0x800000a2","BCO"),
        Entry(163,"0x800000a3","ELLA"),
        Entry(164,"0x800000a4","PIRL"),
        Entry(165,"0x800000a5","XRB"),
        Entry(166,"0x800000a6","VIVO"),
        Entry(167,"0x800000a7","FRST"),
        Entry(168,"0x800000a8","HNC"),
        Entry(169,"0x800000a9","BUZZ"),
        Entry(170,"0x800000aa","MBRS"),
        Entry(171,"0x800000ab","HSR"),
        Entry(172,"0x800000ac","HTML"),
        Entry(173,"0x800000ad","ODN"),
        Entry(174,"0x800000ae","ONX"),
        Entry(175,"0x800000af","RVN"),
        Entry(176,"0x800000b0","GBX"),
        Entry(177,"0x800000b1","BTCZ"),
        Entry(178,"0x800000b2","POA"),
        Entry(179,"0x800000b3","NYC"),
        Entry(180,"0x800000b4","MXT"),
        Entry(181,"0x800000b5","WC"),
        Entry(182,"0x800000b6","MNX"),
        Entry(183,"0x800000b7","BTCP"),
        Entry(184,"0x800000b8","MUSIC"),
        Entry(185,"0x800000b9","BCA"),
        Entry(186,"0x800000ba","CRAVE"),
        Entry(187,"0x800000bb","STAK"),
        Entry(188,"0x800000bc","WBTC"),
        Entry(189,"0x800000bd","LCH"),
        Entry(190,"0x800000be","EXCL"),
        Entry(192,"0x800000c0","LCC"),
        Entry(193,"0x800000c1","XFE"),
        Entry(194,"0x800000c2","EOS"),
        Entry(195,"0x800000c3","TRX"),
        Entry(196,"0x800000c4","KOBO"),
        Entry(197,"0x800000c5","HUSH"),
        Entry(198,"0x800000c6","BANANO"),
        Entry(199,"0x800000c7","ETF"),
        Entry(200,"0x800000c8","OMNI"),
        Entry(201,"0x800000c9","BIFI"),
        Entry(202,"0x800000ca","UFO"),
        Entry(203,"0x800000cb","CNMC"),
        Entry(204,"0x800000cc","BCN"),
        Entry(205,"0x800000cd","RIN"),
        Entry(206,"0x800000ce","ATP"),
        Entry(207,"0x800000cf","EVT"),
        Entry(208,"0x800000d0","ATN"),
        Entry(209,"0x800000d1","BIS"),
        Entry(210,"0x800000d2","NEET"),
        Entry(211,"0x800000d3","BOPO"),
        Entry(212,"0x800000d4","OOT"),
        Entry(213,"0x800000d5","XSPEC"),
        Entry(214,"0x800000d5","MONK"),
        Entry(215,"0x800000d7","BOXY"),
        Entry(216,"0x800000d8","FLO"),
        Entry(217,"0x800000d9","MEC"),
        Entry(218,"0x800000da","BTDX"),
        Entry(219,"0x800000db","XAX"),
        Entry(220,"0x800000dc","ANON"),
        Entry(221,"0x800000dd","LTZ"),
        Entry(222,"0x800000de","BITG"),
        Entry(223,"0x800000df","ASK"),
        Entry(224,"0x800000e0","SMART"),
        Entry(225,"0x800000e1","XUEZ"),
        Entry(226,"0x800000e2","HLM"),
        Entry(227,"0x800000e3","WEB"),
        Entry(228,"0x800000e4","ACM"),
        Entry(229,"0x800000e5","NOS"),
        Entry(230,"0x800000e6","BITC"),
        Entry(231,"0x800000e7","HTH"),
        Entry(232,"0x800000e8","TZC"),
        Entry(233,"0x800000e9","VAR"),
        Entry(234,"0x800000ea","IOV"),
        Entry(235,"0x800000eb","FIO"),
        Entry(236,"0x800000ec","BSV"),
        Entry(237,"0x800000ed","DXN"),
        Entry(238,"0x800000ee","QRL"),
        Entry(239,"0x800000ef","PCX"),
        Entry(240,"0x800000f0","LOKI"),
        Entry(242,"0x800000f2","NIM"),
        Entry(243,"0x800000f3","SOV"),
        Entry(244,"0x800000f4","JCT"),
        Entry(245,"0x800000f5","SLP"),
        Entry(246,"0x800000f6","EWT"),
        Entry(247,"0x800000f7","UC"),
        Entry(248,"0x800000f8","EXOS"),
        Entry(249,"0x800000f9","ECA"),
        Entry(250,"0x800000fa","SOOM"),
        Entry(251,"0x800000fb","XRD"),
        Entry(252,"0x800000fc","FREE"),
        Entry(253,"0x800000fd","NPW"),
        Entry(254,"0x800000fe","BST"),
        Entry(256,"0x80000100","NANO"),
        Entry(257,"0x80000101","BTCC"),
        Entry(259,"0x80000103","ZEST"),
        Entry(260,"0x80000104","ABT"),
        Entry(261,"0x80000105","PION"),
        Entry(262,"0x80000106","DT3"),
        Entry(263,"0x80000107","ZBUX"),
        Entry(264,"0x80000108","KPL"),
        Entry(265,"0x80000109","TPAY"),
        Entry(266,"0x8000010a","ZILLA"),
        Entry(267,"0x8000010b","ANK"),
        Entry(268,"0x8000010c","BCC"),
        Entry(269,"0x8000010d","HPB"),
        Entry(270,"0x8000010e","ONE"),
        Entry(271,"0x8000010f","SBC"),
        Entry(272,"0x80000110","IPC"),
        Entry(273,"0x80000111","DMTC"),
        Entry(274,"0x80000112","OGC"),
        Entry(275,"0x80000113","SHIT"),
        Entry(276,"0x80000114","ANDES"),
        Entry(277,"0x80000115","AREPA"),
        Entry(278,"0x80000116","BOLI"),
        Entry(279,"0x80000117","RIL"),
        Entry(280,"0x80000118","HTR"),
        Entry(281,"0x80000119","FCTID"),
        Entry(282,"0x8000011a","BRAVO"),
        Entry(283,"0x8000011b","ALGO"),
        Entry(284,"0x8000011c","BZX"),
        Entry(285,"0x8000011d","GXX"),
        Entry(286,"0x8000011e","HEAT"),
        Entry(287,"0x8000011f","XDN"),
        Entry(288,"0x80000120","FSN"),
        Entry(289,"0x80000121","CPC"),
        Entry(290,"0x80000122","BOLD"),
        Entry(291,"0x80000123","IOST"),
        Entry(292,"0x80000124","TKEY"),
        Entry(293,"0x80000125","USE"),
        Entry(294,"0x80000126","BCZ"),
        Entry(295,"0x80000127","IOC"),
        Entry(296,"0x80000128","ASF"),
        Entry(297,"0x80000129","MASS"),
        Entry(298,"0x8000012a","FAIR"),
        Entry(299,"0x8000012b","NUKO"),
        Entry(300,"0x8000012c","GNX"),
        Entry(301,"0x8000012d","DIVI"),
        Entry(302,"0x8000012e","CMT"),
        Entry(303,"0x8000012f","EUNO"),
        Entry(304,"0x80000130","IOTX"),
        Entry(305,"0x80000131","ONION"),
        Entry(306,"0x80000132","8BIT"),
        Entry(307,"0x80000133","ATC"),
        Entry(308,"0x80000134","BTS"),
        Entry(309,"0x80000135","CKB"),
        Entry(310,"0x80000136","UGAS"),
        Entry(311,"0x80000137","ADS"),
        Entry(312,"0x80000138","ARA"),
        Entry(313,"0x80000139","ZIL"),
        Entry(314,"0x8000013a","MOAC"),
        Entry(315,"0x8000013b","SWTC"),
        Entry(316,"0x8000013c","VNSC"),
        Entry(317,"0x8000013d","PLUG"),
        Entry(318,"0x8000013e","MAN"),
        Entry(319,"0x8000013f","ECC"),
        Entry(320,"0x80000140","RPD"),
        Entry(321,"0x80000141","RAP"),
        Entry(322,"0x80000142","GARD"),
        Entry(323,"0x80000143","ZER"),
        Entry(324,"0x80000144","EBST"),
        Entry(325,"0x80000145","SHARD"),
        Entry(326,"0x80000146","LINDA"),
        Entry(327,"0x80000147","CMM"),
        Entry(328,"0x80000148","BLOCK"),
        Entry(329,"0x80000149","AUDAX"),
        Entry(330,"0x8000014a","LUNA"),
        Entry(331,"0x8000014b","ZPM"),
        Entry(332,"0x8000014c","KUVA"),
        Entry(333,"0x8000014d","MEM"),
        Entry(334,"0x8000014e","CS"),
        Entry(335,"0x8000014f","SWIFT"),
        Entry(336,"0x80000150","FIX"),
        Entry(337,"0x80000151","CPC"),
        Entry(338,"0x80000152","VGO"),
        Entry(339,"0x80000153","DVT"),
        Entry(340,"0x80000154","N8V"),
        Entry(341,"0x80000155","MTNS"),
        Entry(342,"0x80000156","BLAST"),
        Entry(343,"0x80000157","DCT"),
        Entry(344,"0x80000158","AUX"),
        Entry(345,"0x80000159","USDP"),
        Entry(346,"0x8000015a","HTDF"),
        Entry(347,"0x8000015b","YEC"),
        Entry(348,"0x8000015c","QLC"),
        Entry(349,"0x8000015d","TEA"),
        Entry(350,"0x8000015e","ARW"),
        Entry(351,"0x8000015f","MDM"),
        Entry(352,"0x80000160","CYB"),
        Entry(353,"0x80000161","LTO"),
        Entry(354,"0x80000162","DOT"),
        Entry(355,"0x80000163","AEON"),
        Entry(356,"0x80000164","RES"),
        Entry(357,"0x80000165","AYA"),
        Entry(358,"0x80000166","DAPS"),
        Entry(359,"0x80000167","CSC"),
        Entry(360,"0x80000168","VSYS"),
        Entry(361,"0x80000169","NOLLAR"),
        Entry(362,"0x8000016a","XNOS"),
        Entry(363,"0x8000016b","CPU"),
        Entry(364,"0x8000016c","LAMB"),
        Entry(365,"0x8000016d","VCT"),
        Entry(366,"0x8000016e","CZR"),
        Entry(367,"0x8000016f","ABBC"),
        Entry(368,"0x80000170","HET"),
        Entry(369,"0x80000171","XAS"),
        Entry(370,"0x80000172","VDL"),
        Entry(371,"0x80000173","MED"),
        Entry(372,"0x80000174","ZVC"),
        Entry(379,"0x8000017b","SOX"),
        Entry(384,"0x80000180","XSN"),
        Entry(392,"0x80000188","CENNZ"),
        Entry(398,"0x8000018e","XPC"),
        Entry(400,"0x80000190","NIX"),
        Entry(404,"0x80000194","XBI"),
        Entry(412,"0x8000019c","AIN"),
        Entry(416,"0x800001a0","SLX"),
        Entry(420,"0x800001a4","NODE"),
        Entry(425,"0x800001a9","AION"),
        Entry(426,"0x800001aa","BC"),
        Entry(444,"0x800001bc","PHR"),
        Entry(447,"0x800001bf","DIN"),
        Entry(457,"0x800001c9","AE"),
        Entry(464,"0x800001d0","ETI"),
        Entry(488,"0x800001e8","VEO"),
        Entry(500,"0x800001f4","THETA"),
        Entry(510,"0x800001fe","KOTO"),
        Entry(512,"0x80000200","XRD"),
        Entry(516,"0x80000204","VEE"),
        Entry(518,"0x80000206","LET"),
        Entry(520,"0x80000208","BTCV"),
        Entry(526,"0x8000020e","BU"),
        Entry(528,"0x80000210","YAP"),
        Entry(533,"0x80000215","PRJ"),
        Entry(555,"0x8000022b","BCS"),
        Entry(557,"0x8000022d","LKR"),
        Entry(561,"0x80000231","NTY"),
        Entry(600,"0x80000258","UTE"),
        Entry(618,"0x8000026a","SSP"),
        Entry(625,"0x80000271","EAST"),
        Entry(663,"0x80000297","SFRX"),
        Entry(666,"0x8000029a","ACT"),
        Entry(667,"0x8000029b","PRKL"),
        Entry(668,"0x8000029c","SSC"),
        Entry(698,"0x800002ba","VEIL"),
        Entry(700,"0x800002bc","XDAI"),
        Entry(713,"0x800002c9","XTL"),
        Entry(714,"0x800002ca","BNB"),
        Entry(768,"0x80000300","BALLZ"),
        Entry(777,"0x80000309","BTW"),
        Entry(800,"0x80000320","BEET"),
        Entry(801,"0x80000321","DST"),
        Entry(808,"0x80000328","QVT"),
        Entry(818,"0x80000332","VET"),
        Entry(820,"0x80000334","CLO"),
        Entry(831,"0x8000033f","CRUZ"),
        Entry(886,"0x80000376","ADF"),
        Entry(888,"0x80000378","NEO"),
        Entry(889,"0x80000379","TOMO"),
        Entry(890,"0x8000037a","XSEL"),
        Entry(900,"0x80000384","LMO"),
        Entry(916,"0x80000394","META"),
        Entry(970,"0x800003ca","TWINS"),
        Entry(996,"0x800003e4","OKP"),
        Entry(997,"0x800003e5","SUM"),
        Entry(998,"0x800003e6","LBTC"),
        Entry(999,"0x800003e7","BCD"),
        Entry(1000,"0x800003e8","BTN"),
        Entry(1001,"0x800003e9","TT"),
        Entry(1002,"0x800003ea","BKT"),
        Entry(1024,"0x80000400","ONT"),
        Entry(1111,"0x80000457","BBC"),
        Entry(1120,"0x80000460","RISE"),
        Entry(1122,"0x80000462","CMT"),
        Entry(1128,"0x80000468","ETSC"),
        Entry(1145,"0x80000479","CDY"),
        Entry(1337,"0x80000539","DFC"),
        Entry(1397,"0x80000575","HYC"),
        Entry(1616,"0x80000650","ELF"),
        Entry(1620,"0x80000654","ATH"),
        Entry(1688,"0x80000698","BCX"),
        Entry(1729,"0x800006c1","XTZ"),
        Entry(1776,"0x800006f0","LBTC"),
        Entry(1815,"0x80000717","ADA"),
        Entry(1856,"0x80000743","TES"),
        Entry(1901,"0x8000076d","CLC"),
        Entry(1919,"0x8000077f","VIPS"),
        Entry(1926,"0x80000786","CITY"),
        Entry(1977,"0x800007b9","XMX"),
        Entry(1984,"0x800007c0","TRTL"),
        Entry(1987,"0x800007c3","EGEM"),
        Entry(1989,"0x800007c5","HODL"),
        Entry(1990,"0x800007c6","PHL"),
        Entry(1997,"0x800007cd","POLIS"),
        Entry(1998,"0x800007ce","XMCC"),
        Entry(1999,"0x800007cf","COLX"),
        Entry(2000,"0x800007d0","GIN"),
        Entry(2001,"0x800007d1","MNP"),
        Entry(2017,"0x800007e1","KIN"),
        Entry(2018,"0x800007e2","EOSC"),
        Entry(2019,"0x800007e3","GBT"),
        Entry(2020,"0x800007e4","PKC"),
        Entry(2048,"0x80000800","MCASH"),
        Entry(2049,"0x80000801","TRUE"),
        Entry(2112,"0x80000840","IoTE"),
        Entry(2221,"0x800008ad","ASK"),
        Entry(2301,"0x800008fd","QTUM"),
        Entry(2302,"0x800008fe","ETP"),
        Entry(2303,"0x800008ff","GXC"),
        Entry(2304,"0x80000900","CRP"),
        Entry(2305,"0x80000901","ELA"),
        Entry(2338,"0x80000922","SNOW"),
        Entry(2570,"0x80000a0a","AOA"),
        Entry(2894,"0x80000b4e","REOSC"),
        Entry(3003,"0x80000bbb","LUX"),
        Entry(3030,"0x80000bd6","XHB"),
        Entry(3381,"0x80000d35","DYN"),
        Entry(3383,"0x80000d37","SEQ"),
        Entry(3552,"0x80000de0","DEO"),
        Entry(3564,"0x80000dec","DST"),
        Entry(2718,"0x80000a9e","NAS"),
        Entry(2941,"0x80000b7d","BND"),
        Entry(3276,"0x80000ccc","CCC"),
        Entry(3377,"0x80000d31","ROI"),
        Entry(4218,"0x8000107a","IOTA"),
        Entry(4242,"0x80001092","AXE"),
        Entry(5248,"0x00001480","FIC"),
        Entry(5353,"0x000014e9","HNS"),
        Entry(5920,"0x80001720","SLU"),
        Entry(6060,"0x800017ac","GO"),
        Entry(6666,"0x80001a0a","BPA"),
        Entry(6688,"0x80001a20","SAFE"),
        Entry(6969,"0x80001b39","ROGER"),
        Entry(7777,"0x80001e61","BTV"),
        Entry(8339,"0x80002093","BTQ"),
        Entry(8888,"0x800022b8","SBTC"),
        Entry(8964,"0x80002304","NULS"),
        Entry(8999,"0x80002327","BTP"),
        Entry(9797,"0x80002645","NRG"),
        Entry(9888,"0x800026a0","BTF"),
        Entry(9999,"0x8000270f","GOD"),
        Entry(10000,"0x80002710","FO"),
        Entry(10291,"0x80002833","BTR"),
        Entry(11111,"0x80002B67","ESS"),
        Entry(12345,"0x80003039","IPOS"),
        Entry(13107,"0x80003333","BTY"),
        Entry(13108,"0x80003334","YCC"),
        Entry(15845,"0x80003de5","SDGO"),
        Entry(16754,"0x80004172","ARDR"),
        Entry(19165,"0x80004add","SAFE"),
        Entry(19167,"0x80004adf","ZEL"),
        Entry(19169,"0x80004ae1","RITO"),
        Entry(20036,"0x80004e44","XND"),
        Entry(22504,"0x800057e8","PWR"),
        Entry(25252,"0x800062a4","BELL"),
        Entry(25718,"0x80006476","CHX"),
        Entry(31102,"0x8000797e","ESN"),
        Entry(33416,"0x80008288","TEO"),
        Entry(33878,"0x80008456","BTCS"),
        Entry(34952,"0x80008888","BTT"),
        Entry(37992,"0x80009468","FXTC"),
        Entry(39321,"0x80009999","AMA"),
        Entry(49344,"0x0000C0C0","STASH"),
        Entry(65536,"0x80010000","KETH"),
        Entry(88888,"0x80015b38","RYO"),
        Entry(99999,"0x8001869f","WICC"),
        Entry(200625,"0x80030fb1","AKA"),
        Entry(200665,"0x80011000","GENOM"),
        Entry(246529,"0x8003C301","ATS"),
        Entry(424242,"0x80067932","X42"),
        Entry(666666,"0x800a2c2a","VITE"),
        Entry(1171337,"0x8011df89","ILT"),
        Entry(1313114,"0x8014095a","ETHO"),
        Entry(1712144,"0x801a2010","LAX"),
        Entry(5249353,"0x80501949","BCO"),
        Entry(5249354,"0x8050194a","BHD"),
        Entry(5264462,"0x8050544e","PTN"),
        Entry(5718350,"0x8057414e","WAN"),
        Entry(5741564,"0x80579bfc","WAVES"),
        Entry(7562605,"0x8073656d","SEM"),
        Entry(7567736,"0x80737978","ION"),
        Entry(7825266,"0x80776772","WGR"),
        Entry(7825267,"0x80776773","OBSR"),
        Entry(61717561,"0x83adbc39","AQUA"),
        Entry(91927009,"0x857ab1e1","kUSD"),
        Entry(99999998,"0x85f5e0fe","FLUID"),
        Entry(99999999,"0x85f5e0ff","QKC")
    )

    val byIndex: Map<Int, Entry>

    val byWalletIndex: Map<Long, Entry>

    val bySymbol: Map<String, Entry>

    val byHex: Map<String, Entry>

    init {
        byIndex = FrozenHashMap {
            val map = it
            entries.forEach { map[it.index] = it }
        }

        byWalletIndex = FrozenHashMap {
            val map = it
            entries.forEach { map[it.walletIndex] = it }
        }

        bySymbol = FrozenHashMap {
            val map = it
            entries.forEach { map[it.symbol] = it }
        }

        byHex = FrozenHashMap {
            val map = it
            entries.forEach { map[it.hex] = it }
        }
    }
}
