package jimm.comm;

import jimm.ContactItem;

import java.util.Hashtable;
import java.util.Vector;

import com.tomclaw.xmlgear.XMLGear;
import com.tomclaw.xmlgear.XMLItem;

public class ClientID {
    /**
     * @author Lavlinsky Roman
     */
    static Vector GUIDBased = new Vector();
    static Vector Clients = new Vector();

    public ClientID() {
        /*
        * Открываем файл
        */
        String content = Util.removeCr(Util.getStringAsStream("/clients.xml"));
        if (content.length() == 0) {
            return;
        }
        try {
            /*
            * Обрабатываем файл обработчиком xml
            */
            XMLGear xg = new XMLGear();
            xg.setStructure(content);
            /*
            * Получаем массив капсов
            */
            XMLItem[] tempXML = xg.getItemsWithHeader(new String[]{"caps"}, "cap");
            if (tempXML == null) {
                return;
            }
            for (int i = 0; i < tempXML.length; i++) {
                /*
                * Извлекаем капсы и преобразуем их в байтовые массивы
                */
                GUIDs guids = new GUIDs();
                XMLItem xi = tempXML[i];
                /*
                * Байтовое значение капса
                */
                guids.value = new GUID(Util.explodeToBytesLine(xi.getParamValue("value")));
                /*
                * Внутреннее имя капса
                */
                guids.name = xi.getParamValue("par_name");
                /*
                * Пояснение для капса
                */
                guids.desc = xi.getParamValue("desc");
                /*
                * Добавляем капс в стандартный массив капсов
                */
                GUIDBased.addElement(guids);
//                System.out.println("guids.value = " + xi.getParamValue("value"));
//                System.out.println("guids.name = " + guids.name);
//                System.out.println("guids.desc = " + guids.desc);
            }
            /*
            * Хеш коды используемых значений
            */
            final int cap = "cap".hashCode();
            final int caps = "caps".hashCode();
            final int caps_num = "caps_num".hashCode();
            final int only_caps = "only_caps".hashCode();
            final int nocaps = "no_caps".hashCode();
            final int dc_info1 = "dc_info1".hashCode();
            final int dc_info2 = "dc_info2".hashCode();
            final int dc_info3 = "dc_info3".hashCode();
            final int version = "version".hashCode();
            final int proto = "proto".hashCode();
            /*
            * Получаем массив клиентов
            */
            tempXML = xg.getItemsWithHeader(new String[]{"clients"}, "client");//clients-[]
            if (tempXML == null) {
                throw new Exception();
            }
            XMLItem[] temp;
            /*
            * Перечисляем элементы из "clients"
            */
            for (int i = 0; i < tempXML.length; i++) {
                Client client = new Client();
                /*
                * Элемент "client"
                */
                XMLItem xi = tempXML[i];
                /*
                * Извлекаем номер элемента в clients.png
                */
                client.ClientName = xi.getParamValue("par_name");
                try {
                    client.onImageList = Integer.parseInt(xi.getParamValue("pic"));
                } catch (Exception ignored) {
                }
                /*
                * Получаем элементы текущего клиента
                */
                temp = xg.getItems(xi);
                /*
                * Перечисляем элементы из "client"
                */
                for (int j = 0; j < temp.length; j++) {
                    /*
                    * Переменная элемента из "client" для удобства обработки
                    */
                    XMLItem xi2 = temp[j];
                    /*
                    * Заголовок "caps"
                    */
                    if (xi2.itemHeader.hashCode() == caps) {
                        /*
                        * Получаем элементы из текущего "caps"
                        */
                        XMLItem[] temp2 = xg.getItems(xi2);
                        /*
                        * Перечисляем элементы из "caps"
                        */
                        for (int u = 0; u < temp2.length; u++) {
                            try {
                                /*
                                * Заголовок "cap" в массиве элементов "caps"
                                */
                                if (temp2[u].itemHeader.hashCode() == cap) {
                                    /*
                                    * Перечисляем стандартные капсы
                                    */
                                    for (int f = 0; f < GUIDBased.size(); f++) {
                                        GUIDs capGUID = (GUIDs) GUIDBased.elementAt(f);
                                        /*
                                        * Имя стандартного капса совпадает с полученным именем элемента "cap" из "caps"
                                        */
                                        if (capGUID.name.hashCode() == temp2[u].getParamValue("par_name").hashCode()) {
                                            /*
                                            * Добавляем капс, который должен присутствовать при определении, к текущему клиенту
                                            */
                                            client.caps.addElement(capGUID);
                                        }
                                    }
                                }
                            } catch (Exception ignored) {
                            }
                        }
                        /*
                        * Добавляем метод определения "caps" к текущему "client"
                        */
                        client.methods.addElement("caps");
                        /*
                        * Заголовок "caps_num"
                        */
                    } else if (xi2.itemHeader.hashCode() == caps_num) {
                        /*
                        * Получаем элементы из текущего "caps_num"
                        */
                        XMLItem[] temp2 = xg.getItems(xi2);
                        /*
                        * Перечисляем элементы из "caps_num"
                        */
                        for (int u = 0; u < temp2.length; u++) {
                            try {
                                /*
                                * Заголовок "cap" в массиве элементов "caps_num"
                                */
                                if (temp2[u].itemHeader.hashCode() == cap) {
                                    /*
                                    * Перечисляем стандартные капсы
                                    */
                                    for (int f = 0; f < GUIDBased.size(); f++) {
                                        GUIDs capGUID = (GUIDs) GUIDBased.elementAt(f);
                                        /*
                                        * Имя стандартного капса совпадает с полученным именем элемента "cap" из "caps_num"
                                        */
                                        if (capGUID.name.hashCode() == temp2[u].getParamValue("par_name").hashCode()) {
                                            try {
                                                /*
                                                * Считываем и присваиваем целочисленное положение "num" для элемента "cap" из "caps_num"
                                                */
                                                capGUID.num = (byte) Integer.parseInt(temp2[u].getParamValue("num"));
                                            } catch (Exception ignored) {
                                            }
                                            /*
                                            * Добавляем капс, который должен присутствовать, и находиться в определенном положении, при определении, к текущему клиенту
                                            */
                                            client.caps.addElement(capGUID);
                                        }
                                    }
                                }
                            } catch (Exception ignored) {
                            }
                        }
                        /*
                        * Добавляем метод определения "caps_num" к текущему "client"
                        */
                        client.methods.addElement("caps_num");
                        /*
                        * Заголовок "only_caps"
                        */
                    } else if (xi2.itemHeader.hashCode() == only_caps) {
                        /*
                        * Получаем элементы из текущего "only_caps"
                        */
                        XMLItem[] temp2 = xg.getItems(xi2);
                        /*
                        * Перечисляем элементы из "only_caps"
                        */
                        for (int u = 0; u < temp2.length; u++) {
                            try {
                                /*
                                * Заголовок "cap" в массиве элементов "only_caps"
                                */
                                if (temp2[u].itemHeader.hashCode() == cap) {
                                    /*
                                    * Перечисляем стандартные капсы
                                    */
                                    for (int f = 0; f < GUIDBased.size(); f++) {
                                        GUIDs capGUID = (GUIDs) GUIDBased.elementAt(f);
                                        /*
                                        * Имя стандартного капса совпадает с полученным именем элемента "cap" из "only_caps"
                                        */
                                        if (capGUID.name.hashCode() == temp2[u].getParamValue("par_name").hashCode()) {
                                            /*
                                            * Добавляем капс, который должен присутствовать при определении, к текущему клиенту.. Кроме капсов этого типа не должны присутствовать никакие капсы
                                            */
                                            client.caps.addElement(capGUID);
                                        }
                                    }
                                }
                            } catch (Exception ignored) {
                            }
                        }
                        /*
                        * Добавляем метод определения "only_caps" к текущему "client"
                        */
                        client.methods.addElement("only_caps");
                        /*
                        * Заголовок "nocaps"
                        */
                    } else if (xi2.itemHeader.hashCode() == nocaps) {
                        /*
                        * Получаем элементы из текущего "nocaps"
                        */
                        XMLItem[] temp2 = xg.getItems(xi2);
                        /*
                        * Перечисляем элементы из "nocaps"
                        */
                        for (int u = 0; u < temp2.length; u++) {
                            try {
                                /*
                                * Заголовок "cap" в массиве элементов "nocaps"
                                */
                                if (temp2[u].itemHeader.hashCode() == cap) {
                                    /*
                                    * Перечисляем стандартные капсы
                                    */
                                    for (int f = 0; f < GUIDBased.size(); f++) {
                                        GUIDs capGUID = (GUIDs) GUIDBased.elementAt(f);
                                        /*
                                        * Имя стандартного капса совпадает с полученным именем элемента "cap" из "nocaps"
                                        */
                                        if (capGUID.name.hashCode() == temp2[u].getParamValue("par_name").hashCode()) {
                                            /*
                                            * Добавляем капс, который должен отсутствовать при определении, к текущему клиенту
                                            */
                                            client.nocaps.addElement(capGUID);
                                        }
                                    }
                                }
                            } catch (Exception ignored) {
                            }
                        }
                        /*
                        * Добавляем метод определения "no_caps" к текущему "client"
                        */
                        client.methods.addElement("no_caps");
                        /*
                        * Заголовок "dc_info1"
                        */
                    } else if (xi2.itemHeader.hashCode() == dc_info1) {
                        try {
                            /*
                            * Считываем значение параметра "value" и присваиваем его текущему dc
                            */
                            client.dc1 = Util.extractInt(xi2.getParamValue("value"), 16);
                            /*
                            * Добавляем метод определения "dc_info1" к текущему "client"
                            */
                            client.methods.addElement("dc_info1");
                        } catch (Exception ignored) {
                        }
                        /*
                        * Заголовок "dc_info2"
                        */
                    } else if (xi2.itemHeader.hashCode() == dc_info2) {
                        try {
                            /*
                            * Считываем значение параметра "value" и присваиваем его текущему dc
                            */
                            client.dc2 = Util.extractInt(xi2.getParamValue("value"), 16);
                            /*
                            * Добавляем метод определения "dc_info2" к текущему "client"
                            */
                            client.methods.addElement("dc_info2");
                        } catch (Exception ignored) {
                        }
                        /*
                        * Заголовок "dc_info3"
                        */
                    } else if (xi2.itemHeader.hashCode() == dc_info3) {
                        try {
                            /*
                            * Считываем значение параметра "value" и присваиваем его текущему dc
                            */
                            client.dc3 = Util.extractInt(xi2.getParamValue("value"), 16);
                            /*
                            * Добавляем метод определения "dc_info3" к текущему "client"
                            */
                            client.methods.addElement("dc_info3");
                        } catch (Exception ignored) {
                        }
                    } else
                        /*
                        * Заголовок "proto"
                        */
                        if (xi2.itemHeader.hashCode() == proto) {
                            /*
                            * Считываем и присваиваем значение параметра "version" для элемента "proto"
                            */
                            String ver = xi2.getParamValue("version");
                            client.proto = Integer.parseInt(ver);
                            /*
                            * Добавляем метод по протололу, "proto" к текущему "client"
                            */
                            client.methods.addElement("proto");
                            /*
                            * Заголовок "version"
                            */
                        } else if (xi2.itemHeader.hashCode() == version) {
                            /*
                            * Считываем и присваиваем значение параметра "from" для элемента "version"
                            */
                            String from = xi2.getParamValue("from");
                            /*
                            * Считываем и присваиваем значение параметра "method" для элемента "version"
                            */
                            String method = xi2.getParamValue("method");
                            /*
                            * Параметр "cap" в "method" из "version"
                            */
                            if (method.hashCode() == cap) {
                                /*
                                * Присваиваем клиенту распознавание версии по METHOD_CAP
                                */
                                client.type = Client.METHOD_CAP;
                                /*
                                * Присваиваем клиенту хеш код имени капса с версией для метода распознавания METHOD_CAP
                                */
                                client.nameCapHash = from.hashCode();
                                try {
                                    /*
                                    * Присваиваем клиенту стартовое положение в массиве байтов капса с версией, для метода распознавания METHOD_CAP
                                    */
                                    client.index = Integer.parseInt(xi2.getParamValue("index"));
                                } catch (Exception ignored) {
                                }
                                /*
                                * Параметр "cap_dots" в "method" из "version"
                                */
                            } else if (method.hashCode() == "cap_dots".hashCode()) {
                                /*
                                * Присваиваем клиенту распознавание версии по METHOD_CAP_DOTS
                                */
                                client.type = Client.METHOD_CAP_DOTS;
                                /*
                                * Присваиваем клиенту хеш код имени капса с версией для метода распознавания METHOD_CAP_DOTS
                                */
                                client.nameCapHash = from.hashCode();
                                try {
                                    /*
                                    * Присваиваем клиенту стартовое положение в массиве байтов капса с версией, для метода распознавания METHOD_CAP_DOTS
                                    */
                                    client.index = Integer.parseInt(xi2.getParamValue("index"));
                                } catch (Exception ignored) {
                                }
                                String counts = xi2.getParamValue("count");
                                if (counts == null & client.index > -1) {
                                    /*
                                    * Присваиваем клиенту длину в массиве байтов капса с версией, для метода распознавания METHOD_CAP_DOTS
                                    */
                                    client.count = 16 - client.index;
                                } else if (counts != null & client.index > -1) {
                                    try {
                                        /*
                                        * Присваиваем клиенту длину в массиве байтов капса с версией, для метода распознавания METHOD_CAP_DOTS
                                        */
                                        client.count = Math.min(Integer.parseInt(counts), 16 - client.index);
                                    } catch (Exception ignored) {
                                    }
                                }
                                /*
                                * Параметр "dc_int" в "method" из "version"
                                */
                            } else if (method.hashCode() == "dc_int".hashCode()) {
                                /*
                                * Присваиваем клиенту распознавание версии по METHOD_DC_INT
                                */
                                client.type = Client.METHOD_DC_INT;
                                /*
                                * Просматриваем для какого dc использовать METHOD_DC_INT
                                */
                                if (from.hashCode() == dc_info1) {
                                    client.dc = 1;
                                } else if (from.hashCode() == dc_info2) {
                                    client.dc = 2;
                                } else if (from.hashCode() == dc_info3) {
                                    client.dc = 3;
                                }
                                /*
                                * Параметр "dc_nums" в "method" из "version"
                                */
                            } else if (method.hashCode() == "dc_nums".hashCode()) {
                                /*
                                * Присваиваем клиенту распознавание по METHOD_DC_NUMS
                                */
                                client.type = Client.METHOD_DC_NUMS;
                                /*
                                * Просматриваем для какого dc использовать METHOD_DC_NUMS
                                */
                                if (from.hashCode() == dc_info1) {
                                    client.dc = 1;
                                } else if (from.hashCode() == dc_info2) {
                                    client.dc = 2;
                                } else if (from.hashCode() == dc_info3) {
                                    client.dc = 3;
                                }
                            }
                        }
                }
                /*
                * Добавляем клиент в вектор
                */
                Clients.addElement(client);

                
//                StringBuffer m = new StringBuffer();
//                for (int k = 0; k < client.methods.size(); k++) {
//                    m.append((String) client.methods.elementAt(k)).append(" + ");
//                }
//                System.out.println("\n\nfin client: " + client.ClientName +
//                        "\ncaps: " + client.caps.size() +
//                        "\nnocaps: " + client.nocaps.size() +
//                        "\ncount: " + client.count +
//                        "\nindex: " + client.index +
//                        "\ndc1: " + Integer.toHexString(client.dc1) +
//                        "\ndc2: " + Integer.toHexString(client.dc2) +
//                        "\ndc3: " + Integer.toHexString(client.dc3) +
//                        "\ndc: " + client.dc +
//                        "\nmethods: " + client.methods.size() + " " + m.toString() +
//                        "\ntype: " + client.type +
//                        "\nproto: " + client.proto);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    static boolean thisBest = false;
    static int capsi = 0;

    public static final int CAPF_AIM_SERVERRELAY_INTERNAL = 1 << 0;
    public static final int CAPF_UTF8_INTERNAL = 1 << 1;
    public static final int CAPF_TYPING = 1 << 2;

    public static final String integerPhones[] = Util.explode(
            "Nokia|SonyEricsson|LG|Motorola|Samsung|Siemens|Alcatel"
            , '|'
    );

    public static Vector getClients() {
        return Clients;
    }

    public static String getClientString() {
        StringBuffer sb = new StringBuffer("Jimm").append('|');
        Client client;
        for (int i = 0; i < Clients.size(); i++) {
            if (i > 0) {
                sb.append('|');
            }
            client = (Client) Clients.elementAt(i);
            sb.append(client.ClientName.replace('%', '_'));
        }
        return sb.toString();
    }


    public static void detectClient0(ContactItem cItem, int dwFP1, int dwFP2, int dwFP3, byte[] capabilities, int protocol) {
        /*
        * Первичная иничиализация данных контакта
        */
        boolean temp;
        thisBest = false;
        capsi = 0;
        cItem.capabilities = capabilities;
        cItem.protocol = protocol;
        cItem.dc1 = dwFP1;
        cItem.dc2 = dwFP2;
        cItem.dc3 = dwFP3;
        /*
        * Хеш коды используемых значений
        */
        final int caps = "caps".hashCode();
        final int caps_num = "caps_num".hashCode();
        final int only_caps = "only_caps".hashCode();
        final int nocaps = "no_caps".hashCode();
        final int dc_info1 = "dc_info1".hashCode();
        final int dc_info2 = "dc_info2".hashCode();
        final int dc_info3 = "dc_info3".hashCode();
        final int proto = "proto".hashCode();
        /*
        * Перечисляем имеющиеся клиенты
        */
        for (int i = 0; i < Clients.size(); i++) {
            /*
            * Клиент для проверки
            */
            Client ce = (Client) Clients.elementAt(i);
            /*
            * Сбрасываем переменную, если останется "true" до полного перечисления методов определения клиента, контакту присвоится данный клиент
            */
            temp = true;
            /*
            * Перечисляем имеющиеся методы определения клиента
            */
            for (int j = 0; j < ce.methods.size(); j++) {
                /*
                * Хеш-код имени текущего метода
                */
                int hashMethod = ce.methods.elementAt(j).hashCode();
                /*
                * Текущий метод "caps"
                */
                if (hashMethod == caps) {
                    /*
                    * temp остается "true" если найдены все необходимые капсы
                    */
                    temp &= scanCaps(ce, capabilities);
                    /*
                    * Текущий метод "caps_num"
                    */
                } else if (hashMethod == caps_num) {
                    /*
                    * temp остается "true" если найдены все необходимые капсы, и совпадают их положения с нужными
                    */
                    temp &= scanNumCaps(ce, capabilities);
                    /*
                    * Текущий метод "only_caps"
                    */
                } else if (hashMethod == only_caps) {
                    /*
                    * temp остается "true" если найдены все необходимые капсы, и отсутствуют какие-либо другие
                    */
                    temp &= scanOnlyCaps(ce, capabilities);
                    /*
                    * Текущий метод "nocaps"
                    */
                } else if (hashMethod == nocaps) {
                    /*
                    * temp остается "true" если не найдены все необходимые капсы
                    */
                    temp &= scanNoCaps(ce, capabilities);
                    /*
                    * Текущий метод "proto"
                    */
                } else if (hashMethod == proto) {
                    /*
                    * temp остается "true" если protocol равен присвоенному клиенту proto
                    */
                    temp &= (ce.proto == protocol);
                    /*
                    * Текущий метод "dc_info1"
                    */
                } else if (hashMethod == dc_info1) {
                    /*
                    * temp остается "true" если dwFP1 равен присвоенному клиенту dc1
                    */
                    temp &= (ce.dc1 == dwFP1);
                    /*
                    * Текущий метод "dc_info2"
                    */
                } else if (hashMethod == dc_info2) {
                    /*
                    * temp остается "true" если dwFP2 равен присвоенному клиенту dc2
                    */
                    temp &= (ce.dc2 == dwFP2);
                    /*
                    * Текущий метод "dc_info3"
                    */
                } else if (hashMethod == dc_info3) {
                    /*
                    * temp остается "true" если dwFP3 равен присвоенному клиенту dc3
                    */
                    temp &= (ce.dc3 == dwFP3);
                }
                /*
                * Если temp не равен "true", перечисление методов прерывается, сканируется следующий клиент
                */
                if (!temp) {
                    break;
                }
            }
            if (temp) {
                /*
                * Если temp равен "true", то применяем к контакту необходимые параметры клиента
                */
                /*
                * Положение в clients.png + 1
                */
                cItem.setIntValue(ContactItem.CONTACTITEM_CLIENT_IMAGE, ce.onImageList + 1);
                /*
                * Версия клиента в виде текста
                */
                cItem.setStringValue(ContactItem.CONTACTITEM_CLIVERSION, Util.removeNullChars(ce.getFullVersion(capabilities, dwFP1, dwFP2, dwFP3)));
                /*
                * Сканирование модели телефона
                */
                cItem.setStringValue(ContactItem.CONTACTITEM_MODEL_PHONE, Util.removeNullChars(scanPhone(capabilities, dwFP1)));
                break;
            }
        }
        /*
        * Сканируем капсы для системных функций и thisBest.
        */
        scanWorkCaps(capabilities);
        cItem.setIntValue(ContactItem.CONTACTITEM_CAPABILITIES, capsi);
        cItem.setBooleanValue(ContactItem.CONTACTITEM_CAN1215, thisBest);
        
//#sijapp cond.if modules_DEBUGLOG is "true" #
        /*
        * В консоль внешний вид байтов
        */
        StringBuffer sbs = new StringBuffer();
        sbs.append(cItem.name).append(" dwFP1= ").append(Integer.toHexString(dwFP1)).append(" dwFP2= ")
                .append(Integer.toHexString(dwFP2)).append(" dwFP3= ").append(Integer.toHexString(dwFP3))
                .append("\n\n").append(Util.showBytes(capabilities, 16));

        System.out.println(sbs.toString());
        //jimm.DebugLog.addText(sbs.toString());
//#sijapp cond.end#
    }

    private static void scanWorkCaps(byte[] capabilities) {
        int count = capabilities.length / 16, j16;

        GUID jbest = new GUID(Util.explodeToBytes("4A,5B,69,5D,6D,6D,46,61,6B,65,55,54,46,38,00,00", ',', 16));
        for (int j = 0; j < count; j++) {
            j16 = j * 16;
            if (jbest.equals(capabilities, j16, 6)) {
                thisBest = true;
            }
        }
        
        for (int j = 0; j < count; j++) {
            j16 = j * 16;
            if (GUID.CAP_UTF8.equals(capabilities, j16, 16)) {
                capsi |= CAPF_UTF8_INTERNAL;
                continue;
            }
            if (GUID.CAP_MTN.equals(capabilities, j16, 16)) {
                capsi |= CAPF_TYPING;
                continue;
            }
            if (GUID.CAP_AIM_SERVERRELAY.equals(capabilities, j16, 16)) {
                capsi |= CAPF_AIM_SERVERRELAY_INTERNAL;
                /*continue;*/
            }
        }
    }

    private static boolean scanCaps(Client ce, byte[] capabilities) {
        int size = ce.caps.size(), len;
        int count = capabilities.length / 16, j16;
        boolean temp;
        for (int i = 0; i < size; i++) {
            GUID guid = ((GUIDs) ce.caps.elementAt(i)).value;
            len = guid.toByteArray().length;
            temp = false;
            for (int j = 0; j < count; j++) {
                j16 = j * 16;
                if (guid.equals(capabilities, j16, len)) {
                    temp = true;
                    break;
                }
            }
            if (!temp) {
                return false;
            }
        }
        return true;
    }

    private static boolean scanOnlyCaps(Client ce, byte[] capabilities) {
        int size = ce.caps.size(), len;
        int count = capabilities.length / 16, j16;
        if (size != count) {
            return false;
        }
        boolean temp;
        for (int i = 0; i < size; i++) {
            GUID guid = ((GUIDs) ce.caps.elementAt(i)).value;
            len = guid.toByteArray().length;
            temp = false;
            for (int j = 0; j < count; j++) {
                j16 = j * 16;
                if (guid.equals(capabilities, j16, len)) {
                    temp = true;
                    break;
                }
            }
            if (!temp) {
                return false;
            }
        }
        return true;
    }

    private static boolean scanNumCaps(Client ce, byte[] capabilities) { // todo test
        int count = capabilities.length / 16, j16;
        int size = ce.caps.size(), len;
        for (int i = 0; i < size; i++) {
            byte num = ((GUIDs) ce.caps.elementAt(i)).num;
            GUID guid = ((GUIDs) ce.caps.elementAt(i)).value;
            len = guid.toByteArray().length;
            if (num > count - 1 || num < 0) {
                return false;
            }
            j16 = num * 16;
            if (!guid.equals(capabilities, j16, len)) {
                return false;
            }
        }
        return true;
    }

    private static boolean scanNoCaps(Client ce, byte[] capabilities) {
        int size = ce.nocaps.size(), len;
        int count = capabilities.length / 16, j16;
        for (int i = 0; i < size; i++) {
            GUID guid = ((GUIDs) ce.nocaps.elementAt(i)).value;
            len = guid.toByteArray().length;
            for (int j = 0; j < count; j++) {
                j16 = j * 16;
                if (guid.equals(capabilities, j16, len)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String capsRead(byte[] capabilities, int dc1, int dc2, int dc3) {
        StringBuffer sb = new StringBuffer();
        int size = GUIDBased.size();
        int count = capabilities.length / 16, j16;
        boolean containe;
        for (int j = 0; j < count; j++) {
            j16 = j * 16;
            containe = false;
            for (int i = 0; i < size; i++) {
                GUIDs guids = (GUIDs) GUIDBased.elementAt(i);
                if (guids.value.containsIn(capabilities, j16, 16)) {
                    sb.append(guids.desc).append('\n');
                    containe = true;
                }
            }
            if (!containe) {
                sb.append("[").append(Util.showBytesCompact(capabilities, j16, 16)).append("]").append("\n");
            }
        }
        sb.append("\n");
        String dcs;
        int[] dc = {dc1, dc2, dc3};
        for (int i = 0; i < dc.length; i++) {
            sb.append("DC").append(i/* + 1*/).append(": ");
            dcs = "0000000" + Integer.toHexString(dc[i]).toUpperCase();
            sb.append(dcs.substring(dcs.length() - 8)).append('\n');
        }
        return sb.toString();
    }


    private static String scanPhone(byte[] capabilities, int dwFP1) {
        int count = capabilities.length / 16, j16;

        GUID mi = new GUID(Util.explodeToBytes("6D,69,3A", ',', 16));
        for (int j = 0; j < count; j++) {
            j16 = j * 16;
            if (mi.equals(capabilities, j16, 3)) {
                byte[] buf = new byte[16];
                System.arraycopy(capabilities, j16, buf, 0, 16);
                return getPlatform(Util.byteArrayToString(buf, 3, 13));
            }
        }

        GUID bayan = new GUID(Util.explodeToBytes("*bayanICQ", ',', 16));
        for (int j = 0; j < count; j++) {
            j16 = j * 16;
            if (dwFP1 == 0) break;
            if (bayan.equals(capabilities, j16, 8)) {
                Object key = new Integer(dwFP1);
                String temp;
                temp = (String) resourcesBayan.get(key);
                return getPlatform(temp);
            }
        }
        return "";
    }

    private static String getPlatform(String mobilePhone) {
        if (mobilePhone == null) {
            return null;
        }
        int idx;
        try {
            idx = Integer.parseInt(mobilePhone.substring(0, 1));
        } catch (Exception e) {
            idx = -1;
        }

        if (idx != -1) {
            StringBuffer sb = new StringBuffer();
            sb.append(integerPhones[idx]).append(' ').append(mobilePhone.substring(1));
            mobilePhone = Util.replaceStr(sb.toString(), "TS", " Touch");
        }
        return mobilePhone;
    }

    public static int detectQipStatus(byte[] capabilities) {
        int count = capabilities.length / 16, j16;
        for (int j = 0; j < count; j++) {
            j16 = j * 16;
            if (GUID.STATUS_CHAT.equals(capabilities, j16, 16)) {
                return ContactItem.STATUS_CHAT;
            } else if (GUID.STATUS_DEPRESSION.equals(capabilities, j16, 16)) {
                return ContactItem.STATUS_DEPRESSION;
            } else if (GUID.STATUS_EVIL.equals(capabilities, j16, 16)) {
                return ContactItem.STATUS_EVIL;
            } else if (GUID.STATUS_HOME.equals(capabilities, j16, 16)) {
                return ContactItem.STATUS_HOME;
            } else if (GUID.STATUS_LUNCH.equals(capabilities, j16, 16)) {
                return ContactItem.STATUS_LUNCH;
            } else if (GUID.STATUS_WORK.equals(capabilities, j16, 16)) {
                return ContactItem.STATUS_WORK;
            }
        }
        return ContactItem.STATUS_NONE;
    }

    static private Hashtable resourcesBayan = null;

    static {
        resourcesBayan = new Hashtable();
        resourcesBayan.put(new Integer(0x200005f8), "03250");
        resourcesBayan.put(new Integer(0x20000602), "05500");
        resourcesBayan.put(new Integer(0x20002495), "0E50");
        resourcesBayan.put(new Integer(0x20001856), "0E60");
        resourcesBayan.put(new Integer(0x20001858), "0E61");
        resourcesBayan.put(new Integer(0x20002d7f), "0E61i");
        resourcesBayan.put(new Integer(0x20001859), "0E62");
        resourcesBayan.put(new Integer(0x200025c3), "0E63");
        resourcesBayan.put(new Integer(0x200025c3), "0E65");
        resourcesBayan.put(new Integer(0x2000249c), "0E66");
        resourcesBayan.put(new Integer(0x20001857), "0E70");
        resourcesBayan.put(new Integer(0x200005ff), "0N71");
        resourcesBayan.put(new Integer(0x200005fb), "0N73");
        resourcesBayan.put(new Integer(0x200005fe), "0N75");
        resourcesBayan.put(new Integer(0x20000601), "0N77");
        resourcesBayan.put(new Integer(0x200005f9), "0N80");
        resourcesBayan.put(new Integer(0x20014dd2), "0N86");
        resourcesBayan.put(new Integer(0x200005fc), "0N91");
        resourcesBayan.put(new Integer(0x200005fa), "0N92");
        resourcesBayan.put(new Integer(0x20000600), "0N93");
        resourcesBayan.put(new Integer(0x20000605), "0N93i");
        resourcesBayan.put(new Integer(0x2001de9d), "05530");
        resourcesBayan.put(new Integer(0x2000da56), "05800");
        resourcesBayan.put(new Integer(0x20014ddd), "0N97");
        resourcesBayan.put(new Integer(0x20002d7c), "05700");
        resourcesBayan.put(new Integer(0x20002d7b), "06110");
        resourcesBayan.put(new Integer(0x20002d7e), "06120");
        resourcesBayan.put(new Integer(0x20000606), "06290");
        resourcesBayan.put(new Integer(0x20002498), "0E51");
        resourcesBayan.put(new Integer(0x20014dcc), "0E52");
        resourcesBayan.put(new Integer(0x2000249b), "0E71");
        resourcesBayan.put(new Integer(0x20002496), "0E90");
        resourcesBayan.put(new Integer(0x2000060a), "0N76");
        resourcesBayan.put(new Integer(0x20002d83), "0N81");
        resourcesBayan.put(new Integer(0x20002d85), "0N82");
        resourcesBayan.put(new Integer(0x2000060b), "0N95");
        resourcesBayan.put(new Integer(0x20002d84), "0N95-2");
        resourcesBayan.put(new Integer(0x2000da5a), "05320");
        resourcesBayan.put(new Integer(0x2000da54), "06210");
        resourcesBayan.put(new Integer(0x2000da52), "06220");
        resourcesBayan.put(new Integer(0x2000da57), "06650");
        resourcesBayan.put(new Integer(0x20002d81), "0N78");
        resourcesBayan.put(new Integer(0x2000da64), "0N79");
        resourcesBayan.put(new Integer(0x20002d86), "0N85");
        resourcesBayan.put(new Integer(0x20002d82), "0N96");
        resourcesBayan.put(new Integer(0x2000c51d), "4G810");
        resourcesBayan.put(new Integer(0x2000a677), "4I450");
        resourcesBayan.put(new Integer(0x20003abd), "4I520");
        resourcesBayan.put(new Integer(0x2000a678), "4I550");
        resourcesBayan.put(new Integer(0x2000c51c), "4I560");
        resourcesBayan.put(new Integer(0x2000c520), "4I8910");
        resourcesBayan.put(new Integer(0x2000c51e), "4I8510");
        resourcesBayan.put(new Integer(0x2000c51f), "4I7110");
        resourcesBayan.put(new Integer(0x101ff809), "2KS10");
        resourcesBayan.put(new Integer(0x1020e285), "1P990i");
        resourcesBayan.put(new Integer(0x2000cc6c), "1G900");
        resourcesBayan.put(new Integer(0x2000cc70), "1G700");
        resourcesBayan.put(new Integer(0x10274bfa), "1W950i");
        resourcesBayan.put(new Integer(0x20002e6a), "1W960i");
        resourcesBayan.put(new Integer(0x10274bf9), "1M600");
        resourcesBayan.put(new Integer(0x20002e69), "1P1i");
        resourcesBayan.put(new Integer(0x1027400d), "3Z8");
    }

//
//    public static final int CAPF_NO_INTERNAL = 0;
//    public static final int CAPF_AIM_SERVERRELAY_INTERNAL = 1 << 0;
//    public static final int CAPF_UTF8_INTERNAL = 1 << 1;
//    public static final int CAPF_TRILLIAN = 1 << 2;
//    public static final int CAPF_TRILCRYPT = 1 << 3;
//    public static final int CAPF_SIM = 1 << 4;
//    public static final int CAPF_SIMOLD = 1 << 5;
//    public static final int CAPF_LICQ = 1 << 6;
//    public static final int CAPF_RICHTEXT = 1 << 7;
//    public static final int CAPF_STR20012 = 1 << 8;
//    public static final int CAPF_AIMICON = 1 << 9;
//    public static final int CAPF_AIMCHAT = 1 << 10;
//    public static final int CAPF_XTRAZ = 1 << 11;
//    public static final int CAPF_AIMFILE = 1 << 12;
//    public static final int CAPF_AIMIMIMAGE = 1 << 13;
//    public static final int CAPF_AVATAR = 1 << 14;
//    public static final int CAPF_DIRECT = 1 << 15;
//    public static final int CAPF_TYPING = 1 << 16;
//    public static final int CAPF_FILE_SHARING = 1 << 17;
//    public static final int CAPF_NAT_ICQ = 1 << 18;
//    public static final int CAPF_BUDDY_LIST = 1 << 19;
//
//    public static final byte CLI_NONE = 0;
//    public static final byte CLI_QIP = 1;
//    public static final byte CLI_MIRANDA = 2;
//    public static final byte CLI_ANDRQ = 3;
//    public static final byte CLI_RANDQ = 4;
//    public static final byte CLI_TRILLIAN = 5;
//    public static final byte CLI_SIM = 6;
//    public static final byte CLI_KOPETE = 7;
//    public static final byte CLI_JIMM = 8;
//    public static final byte CLI_STICQ = 9;
//    public static final byte CLI_AGILE = 10;
//    public static final byte CLI_LIBICQ2000 = 11;
//    public static final byte CLI_VMICQ = 12;
//    public static final byte CLI_QIPPDASYM = 13;
//    public static final byte CLI_QIPPDAWIN = 14;
//    public static final byte CLI_QIPINFIUM = 15;
//    public static final byte CLI_ICQ6 = 16;
//    public static final byte CLI_ICQLITE = 17;
//    public static final byte CLI_ICQLITE4 = 18;
//    public static final byte CLI_ICQLITE5 = 19;
//    public static final byte CLI_ICQ2003B = 20;
//    public static final byte CLI_ICQ2GO = 21;
//    public static final byte CLI_MCHAT = 22;
//    public static final byte CLI_MACICQ = 23;
//    public static final byte CLI_GAIM = 24;
//    public static final byte CLI_PIGEON = 25;
//    public static final byte CLI_LICQ = 26;
//    public static final byte CLI_MIP = 27;
//    public static final byte CLI_YAPP = 28;
//    public static final byte CLI_SMAPE = 29;
//    public static final byte CLI_DICHAT = 30;
//    public static final byte CLI_LOCID = 31;
//    public static final byte CLI_JIMM_RC = 32;
//    public static final byte CLI_BAYAN = 33;
//    public static final byte CLI_ICQ7 = 34;
//    public static final byte CLI_WJIMM = 35;
//    public static final byte CLI_MANDARIN = 36;
//    public static final byte CLI_IMPLUS = 37;
//    public static final byte CLI_MAGENT = 38;
//    public static final byte CLI_SLICK = 39;
//    public static final byte CLI_GNOMEICQ = 40;
//    public static final byte CLI_QUTIM = 41;
//    public static final byte CLI_AIM = 42;
//    public static final byte CLI_ARXBOT = 43;
//
//
//    public static String getClientString(byte cli) {
//        return Util.explode(
//                "Unknown|QIP|Miranda|&RQ|R&Q|Trillian|SIM|Kopete|Jimm|StICQ|Agile Messenger|Libicq2000|VmICQ|QIP PDA (Symbian)|QIP PDA (Windows)|QIP|ICQ|ICQ Lite|ICQ Lite v4|ICQ Lite v5|" +
//                        "ICQ 2003b|ICQ2GO!|mChat|Mac ICQ|Gaim|PIGEON!|LICQ|MIP|Yapp|Sm@peR|Jimm Dichat|Jimm LocID|Jimm RC|BayanICQ|ICQ v7|wJimm|Mandarin|IM+|Mail.ru Agent|Slick|" +
//                        "Gnome ICQ|QutIM|AIM|ARX-Bot|",
//                '|'
//        )[cli];
//    }
//
//
//    private static String getModelBayan(int dwFP1) {
//        Object key = new Integer(dwFP1);
//        String temp;
//        temp = (String) resourcesBayan.get(key);
//        return temp;
//    }

//
//    public static void detectClient(ContactItem cItem, int dwFP1, int dwFP2, int dwFP3, byte[] capabilities, int wVersion) {
//        int caps = getCaps(capabilities);
//        int client = CLI_NONE;
//
//        String phone = "";
//        StringBuffer version = new StringBuffer();
//
//        if (capabilities != null) {
//            client = detectClientByCapabilities(capabilities, dwFP1, dwFP2, version, caps);
//            if (client == CLI_NONE) {
//                client = detectClientByCombCaps(caps, dwFP1, dwFP2, dwFP3, wVersion, version);
//            }
//            phone = detectPhoneByCapabilities(capabilities);
//        }
//        if (client == CLI_NONE) {
//            client = detectClientByFP(dwFP1, dwFP2, dwFP3, wVersion, version);
//        }
//
//        if (client == CLI_BAYAN && phone.length() < 1 && dwFP1 != 0) {
//            phone = getPlatform(getModelBayan(dwFP1));
//        }
//
////#sijapp cond.if modules_DEBUGLOG is "true" #
//        StringBuffer sbs = new StringBuffer();
//        sbs.append(cItem.name).append(" dwFP1= ").append(Integer.toHexString(dwFP1)).append(" dwFP2= ")
//                .append(Integer.toHexString(dwFP2)).append(" dwFP3= ").append(Integer.toHexString(dwFP3))
//                .append("\n\n").append(Util.showBytes(capabilities, 16));
//
//        //jimm.DebugLog.addText(sbs.toString());
//        System.out.println(sbs.toString());
////#sijapp cond.end#
//
//        cItem.setBooleanValue(ContactItem.CONTACTITEM_CAN1215, thisBest);
//        cItem.setIntValue(ContactItem.CONTACTITEM_CAPABILITIES, caps);
//        cItem.setIntValue(ContactItem.CONTACTITEM_CLIENT, client);
//        cItem.setStringValue(ContactItem.CONTACTITEM_CLIVERSION, Util.removeNullChars(version.toString()));
//        cItem.setStringValue(ContactItem.CONTACTITEM_MODEL_PHONE, Util.removeNullChars(phone));
//    }
//
//    private static int getCaps(byte[] capabilities) {
//        int caps = CAPF_NO_INTERNAL;
//
//        if (GUID.CAP_AIM_SERVERRELAY.containsIn(capabilities)) {
//            caps |= CAPF_AIM_SERVERRELAY_INTERNAL;
//        }
//        if (GUID.CAP_UTF8.containsIn(capabilities)) {
//            caps |= CAPF_UTF8_INTERNAL;
//        }
//        if (GUID.CAP_TRILLIAN.containsIn(capabilities)) {
//            caps |= CAPF_TRILLIAN;
//        }
//        if (GUID.CAP_TRILCRYPT.containsIn(capabilities)) {
//            caps |= CAPF_TRILCRYPT;
//        }
//        if (GUID.CAP_SIM.containsIn(capabilities)) {
//            caps |= CAPF_SIM;
//        }
//        if (GUID.CAP_SIMOLD.containsIn(capabilities)) {
//            caps |= CAPF_SIMOLD;
//        }
//        if (GUID.CAP_MSGTYPE2.containsIn(capabilities)) {
//            caps |= CAPF_LICQ;
//        }
//        if (GUID.CAP_RICHTEXT.containsIn(capabilities)) {
//            caps |= CAPF_RICHTEXT;
//        }
//        if (GUID.CAP_STR20012.containsIn(capabilities)) {
//            caps |= CAPF_STR20012;
//        }
//        if (GUID.CAP_AIMICON.containsIn(capabilities)) {
//            caps |= CAPF_AIMICON;
//        }
//        if (GUID.CAP_AIMCHAT.containsIn(capabilities)) {
//            caps |= CAPF_AIMCHAT;
//        }
//        if (GUID.CAP_XTRAZ.containsIn(capabilities)) {
//            caps |= CAPF_XTRAZ;
//        }
//        if (GUID.CAP_FILE_TRANSFER.containsIn(capabilities)) {
//            caps |= CAPF_AIMFILE;
//        }
//        if (GUID.CAP_AIMIMIMAGE.containsIn(capabilities)) {
//            caps |= CAPF_AIMIMIMAGE;
//        }
//        if (GUID.CAP_AVATAR.containsIn(capabilities)) {
//            caps |= CAPF_AVATAR;
//        }
//        if (GUID.CAP_FILE_TRANSFER.containsIn(capabilities)) {
//            caps |= CAPF_DIRECT;
//        }
//        if (GUID.CAP_MTN.containsIn(capabilities)) {
//            caps |= CAPF_TYPING;
//        }
//        if (GUID.CAP_FILE_SHARING.containsIn(capabilities)) {
//            caps |= CAPF_FILE_SHARING;
//        }
//        if (GUID.CAP_NAT_ICQ.containsIn(capabilities)) {
//            caps |= CAPF_NAT_ICQ;
//        }
//        if (GUID.CAP_BUDDY_LIST.containsIn(capabilities)) {
//            caps |= CAPF_BUDDY_LIST;
//        }
//        return caps;
//    }
//
//    private static String detectPhoneByCapabilities(byte[] capabilities) {
//        int count = capabilities.length / 16, j16;
//        StringBuffer platform = new StringBuffer();
//
//        thisBest = false;
//        for (int j = 0; j < count; j++) {
//            j16 = j * 16;
//            if (GUID.CAP_MODEL.equals(capabilities, j16, 3)) {
//                byte[] buf = new byte[16];
//                System.arraycopy(capabilities, j16, buf, 0, 16);
//                platform.append(getPlatform(Util.byteArrayToString(buf, 3, 13)));
//            }
//            if (GUID.CAP_ECONOM.equals(capabilities, j16, 6)) {
//                thisBest = true;
//            }
//        }
//        return platform.toString();
//    }
//
//    private static int detectClientByCapabilities(byte[] capabilities, int dwFP1, int dwFP2, StringBuffer version, int caps) {
//        int count = capabilities.length / 16, j16;
//        for (int j = 0; j < count; j++) {
//            j16 = j * 16;
//            if (GUID.CAP_MIRANDAIM.equals(capabilities, j16, 8)) {
//                String pre = null;
//                if (version.length() > 0) {
//                    pre = version.toString();
//                    version.setLength(0);
//                }
//                detectClientVersion(version, capabilities, CLI_MIRANDA, j);
//                if (pre != null) {
//                    version.append(pre);
//                }
//                if (pre != null || !GUID.CAP_MIM.containsIn(capabilities, 4)) {
//                    return CLI_MIRANDA;
//                }
//                continue;
//            }
//            if (GUID.CAP_SIM.equals(capabilities, j16, 0xC)) {
//                return CLI_SIM;
//            }
//            if (GUID.CAP_SIMOLD.equals(capabilities, j16, 16)) {
//                return CLI_SIM;
//            }
//            if (GUID.CAP_LICQ.equals(capabilities, j16, 0xC)) {
//                detectClientVersion(version, capabilities, CLI_LICQ, j);
//                return CLI_LICQ;
//            }
//            if (GUID.CAP_KOPETE.equals(capabilities, j16, 0xC)) {
//                detectClientVersion(version, capabilities, CLI_KOPETE, j);
//                return CLI_KOPETE;
//            }
//            if (GUID.CAP_ANDRQ.equals(capabilities, j16, 9)) {
//                detectClientVersion(version, capabilities, CLI_ANDRQ, j);
//                return CLI_ANDRQ;
//            }
//            if (GUID.CAP_QIP.equals(capabilities, j16, 16)) {
//                detectClientVersion(version, capabilities, CLI_QIP, j);
//                if (((dwFP1 >> 24) & 0xFF) != 0) {
//                    version.append(" (")
//                            .append((dwFP1 >> 24) & 0xFF).append((dwFP1 >> 16) & 0xFF).append((dwFP1 >> 8) & 0xFF).append(dwFP1 & 0xFF)
//                            .append(")");
//                }
//                return CLI_QIP;
//            }
//            if (GUID.CAP_QIPINFIUM.equals(capabilities, j16, 16)) {
//                version.append("Infium");
//                if ((dwFP1 & 0xFFFF) != 0) version.append(' ').append('(').append((dwFP1 & 0xFFFF)).append(')');
//                return CLI_QIPINFIUM;
//            }
//            if (GUID.CAP_QIP2010.equals(capabilities, j16, 16)) {
//                version.append("2010a");
//                if ((dwFP1 & 0xFFFF) != 0) version.append(' ').append('(').append((dwFP1 & 0xFFFF)).append(')');
//                return CLI_QIPINFIUM;
//            }
//            if (GUID.CAP_IMPLUS.equals(capabilities, j16, 16)) {
//                return CLI_IMPLUS;
//            }
//            //if (GUID.CAP_IM2.equals(capabilities, j16, 16)) {
//            //    return CLI_IM2;
//            //}
//            if (GUID.CAP_MACICQ.equals(capabilities, j16, 16)) {
//                return CLI_MACICQ;
//            }
//            if (GUID.CAP_VMICQ.equals(capabilities, j16, 6)) {
//                detectClientVersion(version, capabilities, CLI_VMICQ, j);
//                return CLI_VMICQ;
//            }
//            if (GUID.CAP_QIPPDASYM.equals(capabilities, j16, 16)) {
//                return CLI_QIPPDASYM;
//            }
//            if (GUID.CAP_AIMCHAT.equals(capabilities, j16, 16)) {
//                if (checkICQ65(getCaps(capabilities))) {
//                    version.append("v6.5");
//                    return CLI_ICQ6;
//                }
//                if (checkICQ7(getCaps(capabilities))) {
//                    return CLI_ICQ7;
//                }
//                return CLI_AIM;
//            }
//            if (GUID.CAP_ICQ6.equals(capabilities, j16, 16)) {
//                if ((caps & CAPF_AIMCHAT) != 0) {
//                    version.append("v6");
//                    return CLI_ICQ6;
//                }
//                version.setLength(0);
//                return CLI_MAGENT;
//            }
//            if (GUID.CAP_QIPPDAWIN.equals(capabilities, j16, 16)) {
//                return CLI_QIPPDAWIN;
//            }
//            if (GUID.CAP_JIMM.equals(capabilities, j16, 5)) {
//                detectClientVersion(version, capabilities, CLI_JIMM, j);
//                return CLI_JIMM;
//            }
//            if (GUID.CAP_WJIMM.equals(capabilities, j16, 6)) {
//                detectClientVersion(version, capabilities, CLI_JIMM, j);
//                return CLI_WJIMM;
//            }
//            if (GUID.CAP_MIR_ICQP.equals(capabilities, j16, 4)) {
//                String pre = null;
//                if (version.length() > 0) {
//                    pre = version.toString();
//                    version.setLength(0);
//                }
//                int idx = j * 16;
//                makeVersion(version, capabilities[idx + 4] & 0x7F, capabilities[idx + 5], -1, -1);
//                if ((capabilities[idx + 0x4] & 0x80) != 0) version.append(" alpha build #");
//                else version.append('.').append(capabilities[idx + 6]).append('.');
//                version.append(capabilities[idx + 7]).append(" (ICQ Plus ");
//                makeVersion(version, (dwFP2 >> 24) & 0x7F, (dwFP2 >> 16) & 0xFF, (dwFP2 >> 8) & 0xFF, dwFP2 & 0xFF);
//                version.append(')');
//                if (pre != null) {
//                    version.append(pre);
//                }
//                if (pre != null || !GUID.CAP_MIM.containsIn(capabilities, 4)) {
//                    return CLI_MIRANDA;
//                }
//                continue;
//            }
//            if (GUID.CAP_MCHAT.equals(capabilities, j16, 9)) {
//                detectClientVersion(version, capabilities, CLI_MCHAT, j);
//                return CLI_MCHAT;
//            }
//            if (GUID.CAP_MIP.equals(capabilities, j16, 4)) {
//                detectClientVersion(version, capabilities, CLI_MIP, j);
//                return CLI_MIP;
//            }
//            if (GUID.CAP_YAPP.equals(capabilities, j16, 4)) {
//                detectClientVersion(version, capabilities, CLI_YAPP, j);
//                return CLI_YAPP;
//            }
//            if (GUID.CAP_SMAPE.equals(capabilities, j16, 5)) {
//                version.append('v').append(new String(capabilities, j16 + 8, 6));
//                return CLI_SMAPE;
//            }
//            if (GUID.CAP_BAYAN.equals(capabilities, j16, 8)) {
//                version.append('v').append(new String(capabilities, j16 + 8, 8));
//                return CLI_BAYAN;
//            }
//            if (GUID.CAP_PIGEON.equals(capabilities, j16, 7)) {
//                return CLI_PIGEON;
//            }
//            if (GUID.CAP_ARXBOT.equals(capabilities, j16, 8)) {
//                version.append(Util.byteArrayToString(capabilities, j16 + 8, 8));
//                return CLI_ARXBOT;
//            }
//            if (GUID.CAP_MANDARIN.equals(capabilities, j16, 11)) {
//                if (dwFP2 != 0) {
//                    version.append("v").append((dwFP2 >> 24) & 0x0F).append((dwFP2 >> 16) & 0x0F).append((dwFP2 >> 8) & 0x0F).append((dwFP2) & 0x0F);
//                }
//                return CLI_MANDARIN;
//            }
//            if (GUID.CAP_MAILRUS.equals(capabilities, j16, 4)) {
//                version/*.append("Symbian ")*/.append(Util.byteArrayToString(capabilities, j16 + 5, 4));
//                return CLI_MAGENT;
//            }
//            if (GUID.CAP_QUTIM.equals(capabilities, j16, 5)) {
//                int i0 = j16 + 6;
//                char ch = (char) (capabilities[j16 + 5] & 0xFF);
//                version.append('v');
//                if (ch >= '0' && ch <= '9') {
//                    version.append((char) capabilities[--i0]).append((char) capabilities[++i0]).append((char) capabilities[++i0]);
//                } else {
//                    makeVersion(version, capabilities[i0++] & 0xFF, capabilities[i0++] & 0xFF, capabilities[i0++] & 0xFF, -1);
//                    if (capabilities[++i0] != 0) {
//                        version.append(" svn").append(capabilities[i0] & 0xFF);
//                    }
//                    version.append(" (");
//                    switch (ch) {
//                        case 'l':
//                            version.append("Linux");
//                            break;
//                        case 'w':
//                            version.append("Windows");
//                            break;
//                        case 'm':
//                            version.append("Mac OS X");
//                            break;
//                    }
//                    version.append(')');
//                }
//                return CLI_QUTIM;
//            }
//            if (GUID.CAP_MIM.equals(capabilities, j16, 4)) {
//                boolean flag = (version.length() != 0);
//                version.append(" [").append(Util.byteArrayToString(capabilities, j16 + 4, 12)).append(']');
//                if (flag) {
//                    return CLI_MIRANDA;
//                }
//                continue;
//            }
//            if (GUID.CAP_DICHAT.equals(capabilities, j16, 9)) {
//                version.append("v").append(Util.byteArrayToString(capabilities, j16 + 11, 4));
//                return CLI_DICHAT;
//                //continue;
//            }
//        }
//        return CLI_NONE;
//    }
//
//    private static int detectClientByCombCaps(int caps, int dwFP1, int dwFP2, int dwFP3, int wVersion, StringBuffer version) {
//        if (wVersion == 31337) { //QIP's -?- in Client ID
//            if ((caps & CAPF_UTF8_INTERNAL) != 0) {
//                version.append("Infium");
//                return CLI_QIPINFIUM;
//            }
//            version.setLength(0);
//            version.append("2005a");
//            return CLI_QIP;
//        }
//        if (((caps & (CAPF_TRILLIAN + CAPF_TRILCRYPT)) != 0) /*&& (dwFP1 == 0x3b75ac09)*/) {
//            return CLI_TRILLIAN;
//        }
//        if ((caps & (CAPF_SIM + CAPF_SIMOLD)) != 0) {
//            return CLI_SIM;
//        }
//        if (((caps & CAPF_AIMICON) != 0) && ((caps & CAPF_AIMFILE) != 0) && ((caps & CAPF_AIMIMIMAGE) != 0)) {
//            return CLI_GAIM;
//        }
//        if ((caps & CAPF_UTF8_INTERNAL) != 0) {
//            if ((caps & CAPF_TYPING) != 0 && (caps & CAPF_XTRAZ) != 0 && (caps & CAPF_AIM_SERVERRELAY_INTERNAL) != 0
//                    && (caps & CAPF_DIRECT) != 0 && (caps & CAPF_BUDDY_LIST) != 0) {
//                if (version.length() > 0) {
//                    return CLI_NONE;
//                }
//                version.append("(Symbian)");
//                return CLI_MAGENT;
//            }
//            switch (wVersion) {
//                case 10:
//                    if (((caps & CAPF_TYPING) != 0) && ((caps & CAPF_RICHTEXT) != 0)) {
//                        return CLI_ICQ2003B;
//                    }
//
//                case 7:
//                    if (((caps & CAPF_AIM_SERVERRELAY_INTERNAL) == 0) && ((caps & CAPF_DIRECT) == 0) && (dwFP1 == 0) && (dwFP2 == 0) && (dwFP3 == 0)) {
//                        return CLI_ICQ2GO;
//                    }
//                    break;
//
//                default:
//                    if ((dwFP1 == 0) && (dwFP2 == 0) && (dwFP3 == 0)) {
//                        if ((caps & CAPF_RICHTEXT) != 0) {
//                            int client = CLI_ICQLITE;
//                            if (((caps & CAPF_AVATAR) != 0) && ((caps & CAPF_XTRAZ) != 0)) {
//                                if ((caps & CAPF_AIMFILE) != 0) {
//                                    client = CLI_ICQLITE5;
//                                } else {
//                                    client = CLI_ICQLITE4;
//                                }
//                            }
//                            return client;
//                        } else if ((caps & CAPF_FILE_SHARING) != 0) {
//                            return CLI_SLICK;
//                            //} else if ((caps & CAPF_NAT_ICQ) != 0) {
//                            //    return CLI_NATICQ;
//                        } else {
//                            return CLI_AGILE;
//                        }
//                    }
//                    break;
//            }
//        }
//        //if ((dwFP1 != 0) && (dwFP1 == dwFP3) && (dwFP3 == dwFP2) && (caps == CAPF_NO_INTERNAL)) {
//        //    return CLI_VICQ;
//        //}
//        //if (((caps & (CAPF_STR20012 + CAPF_AIM_SERVERRELAY_INTERNAL)) != 0)) {
//        //    if ((dwFP1 == 0) && (dwFP2 == 0) && (dwFP3 == 0) && (wVersion == 0)) {
//        //        return CLI_ICQPPC;
//        //    }
//        //}
//        if (wVersion == 7) {
//            if (((caps & CAPF_AIM_SERVERRELAY_INTERNAL) != 0) && ((caps & CAPF_DIRECT) != 0)) {
//                if ((dwFP1 == 0) && (dwFP2 == 0) && (dwFP3 == 0)) {
//                    return CLI_ANDRQ;
//                }
//            } else if ((caps & CAPF_RICHTEXT) != 0) {
//                return CLI_GNOMEICQ;
//            }
//        }
//        return CLI_NONE;
//    }
//
//    private static int detectClientByFP(int dwFP1, int dwFP2, int dwFP3, int wVersion, StringBuffer version) {
//        if ((dwFP1 & 0xFF7F0000) == 0x7D000000) {
//            int ver = dwFP1 & 0xFFFF;
//            if (ver % 10 != 0) {
//                makeVersion(version, ver / 1000, (ver / 10) % 100, ver % 10, -1);
//            } else {
//                makeVersion(version, ver / 1000, (ver / 10) % 100, -1, -1);
//            }
//            return CLI_LICQ;
//        }
//        switch (dwFP1) {
//            case 0xFFFFFFFF:
//                if ((dwFP3 == 0xFFFFFFFF) && (dwFP2 == 0xFFFFFFFF)) {
//                    return CLI_GAIM;
//                }
//                //if ((dwFP2 == 0) && (dwFP3 != 0xFFFFFFFF)) {
//                //    if (wVersion == 7) {
//                //        return CLI_WEBICQ;
//                //    }
//                //    if ((dwFP3 == 0x3B7248ED) && ((caps & CAPF_UTF8_INTERNAL) == 0) && ((caps & CAPF_RICHTEXT) == 0)) {
//                //        return CLI_SPAM;
//                //    }
//                //}
//                break;
//
//            case 0xFFFFFFFE:
//                if (dwFP3 == dwFP1) {
//                    return CLI_JIMM;
//                }
//                break;
//
//            case 0xEFFEEFFE:
//                if (dwFP2 == 0x00010000 && dwFP3 == 0xEFFEEFFE) {
//                    return CLI_WJIMM;
//                }
//                break;
//
//            case 0x48200903:
//                if (dwFP3 == dwFP1) {
//                    //version.append("RC ").append((dwFP2 >> 8) & 0x0F).append('.').append((dwFP2 >> 4) & 0x0F).append(" (best)");
//                    //return CLI_JIMM;
//                    version.append((dwFP2 >> 8) & 0x0F).append('.').append((dwFP2 >> 4) & 0x0F);
//                    return CLI_JIMM_RC;
//                }
//                break;
//
//            case 0x77766666:
//                if (dwFP3 == dwFP1) {
//                    version.append("dimm v").append((dwFP2 >> 8) & 0x0F).append('.').append((dwFP2 >> 4) & 0x0F).append(((dwFP2) & 0x0F));
//                    return CLI_JIMM;
//                }
//                break;
//
//            case 0x88888888:
//                if (dwFP3 == dwFP1) {
//                    //version.append('v');
//                    //version.append("D[i]chat v");
//                    makeVersion(version, (dwFP2 >> 8) & 0x0F, ((dwFP2 >> 4) & 0x0F) * 10 + (dwFP2 & 0x0F), -1, -1);
//                    if (dwFP2 == 0x00000060 || dwFP2 == 0x00000064)
//                        version.append(" gTouch");
//                    else if (dwFP2 == 0x00000065)
//                        version.append(" eXplorer");
//                    else if (dwFP2 == 0x00000071)
//                        version.append(" nuERA");
//                    else if (dwFP2 == 0x00000076)
//                        version.append(" RevoLT");
//                    else if (dwFP2 == 0x00000078)
//                        version.append(" BLitzkRieG");
//                    else if (dwFP2 == 0x00000079)
//                        version.append(" uDA");
//                    return CLI_DICHAT;
//                    //return CLI_JIMM;
//                }
//                break;
//
////            case 0xFFFFFF8F:
////                return CLI_STRICQ;
////            case 0xFFFFFF42:
////                return CLI_MICQ;
////            case 0xFFFFFFBE:
////                return CLI_ALICQ;
//
//            case 0x77657868:
//                if (dwFP2 != 0)
//                    version.append("v").append((dwFP2 >> 24) & 0x0F).append((dwFP2 >> 16) & 0x0F).append((dwFP2 >> 8) & 0x0F).append((dwFP2) & 0x0F);
//                return CLI_MANDARIN;
//
//            case 0xFFFFFF7F:
//                version.setLength(0);
//                makeVersion(version, (dwFP2 >> 24) & 0xFF, (dwFP2 >> 16) & 0xFF, (dwFP2 >> 8) & 0xFF, dwFP2 & 0xFF);
//                return CLI_ANDRQ;
//
//            case 0xFFFFF666:
//                version.append(dwFP2 & 0xFFFF);
//                return CLI_RANDQ;
//
////            case 0xFFFFFFAB:
////                return CLI_YSM;
////            case 0x04031980:
////                return CLI_VICQ;
//
//            case 0x3AA773EE:
//                if ((dwFP2 == 0x3AA66380) && (dwFP3 == 0x3A877A42)) {
////                    if (wVersion == 7) {
////                        if (((caps & CAPF_AIM_SERVERRELAY_INTERNAL) != 0) && ((caps & CAPF_DIRECT) != 0)) {
////                            if ((caps & CAPF_RICHTEXT) != 0) {
////                                return CLI_CENTERICQ;
////                            }
////                            return CLI_LIBICQJABBER;
////                        }
////                    }
//                    return CLI_LIBICQ2000;
//                }
//                break;
//
//            case 0x3b75ac09:
//                return CLI_TRILLIAN;
//
//            case 0x3BA8DBAF: // FP2: 0x3BEB5373; FP3: 0x3BEB5262;
//                if (wVersion == 2) return CLI_STICQ;
//                break;
//
////            case 0x3FF19BEB:
////                if ((wVersion == 8) && (dwFP1 == dwFP3)) //FP2: 0x3FEC05EB; FP3: 0x3FF19BEB;
////                    return CLI_IM2;
////                break;
////
////            case 0x4201F414:
////                if (((dwFP2 & dwFP3) == dwFP1) && (wVersion == 8)) return CLI_SPAM;
////                break;
//
//            case 0x48151623:
//                if (dwFP3 == dwFP1) {
//                    if (dwFP2 == 0x48151623) {
//                        version.append("final");
//                        return CLI_LOCID;
//                    } else {
//                        //version.append("beta [");
//                        //version.append("LocID v");
//                        version.append('v');
//                        makeVersion(version, (dwFP2 >> 8) & 0x0F, (dwFP2 >> 4) & 0x0F, dwFP2 & 0x0F, -1);
//                        int plugVer = ((dwFP2 & 0x7FFF0000) >> 16);
//                        if (plugVer != 0) {
//                            version.append(" p").append(plugVer);
//                        }
//                        //version.append("]");
//                    }
//                    return CLI_LOCID;
//                    //return CLI_JIMM;
//                }
//
//
//            default:
//                break;
//        }
//        if (dwFP1 > 0x35000000 && dwFP1 < 0x40000000) {
//            switch (wVersion) {
//                case 9:
//                    return CLI_ICQLITE;
//                case 10:
//                    return CLI_ICQ2003B;
//            }
//        }
//        return CLI_NONE;
//    }
//
//    private static void makeVersion(StringBuffer ver, int v0, int v1, int v2, int v3) {
//        ver.append(v0).append('.').append(v1);
//        if (v2 >= 0) ver.append('.').append(v2);
//        if (v3 >= 0) ver.append('.').append(v3);
//    }
//
//    private static boolean checkICQ65(int caps) {
//        return ((caps & CAPF_AIMCHAT) != 0 && (caps & CAPF_AIMFILE) != 0 && (caps & CAPF_UTF8_INTERNAL) != 0
//                && (caps & CAPF_XTRAZ) != 0 && (caps & CAPF_AIM_SERVERRELAY_INTERNAL) != 0 && (caps & CAPF_TYPING) != 0);
//    }
//
//    private static boolean checkICQ7(int caps) {
//        return ((caps & CAPF_AIMCHAT) != 0 && (caps & CAPF_AIMFILE) != 0 && (caps & CAPF_UTF8_INTERNAL) != 0
//                && (caps & CAPF_XTRAZ) != 0 && (caps & CAPF_AIM_SERVERRELAY_INTERNAL) == 0);
//    }
//
//    private static void detectClientVersion(StringBuffer ver, byte[] buf1, int cli, int tlvNum) {
//        byte[] buf = new byte[16];
//        System.arraycopy(buf1, tlvNum * 16, buf, 0, 16);
//        ver.setLength(0);
//        if (cli == CLI_MIRANDA) {
//            if ((buf[0xC] == 0) && (buf[0xD] == 0) && (buf[0xE] == 0) && (buf[0xF] == 1)) {
//                ver.setLength(0);
//                ver.append("0.1.2.0");
//            } else if ((buf[0xC] == 0) && (buf[0xD] <= 3) && (buf[0xE] <= 3) && (buf[0xF] <= 1)) {
//                makeVersion(ver, 0, buf[0xD], buf[0xE], buf[0xF]);
//            } else {
//                makeVersion(ver, buf[0x8] & 0x7F, buf[0x9], -1, -1);
//                if (buf[0x9] == 0x08 && buf[0xA] == 0x00) ver.append(" alpha build #").append(buf[0xB]);
//                else ver.append('.').append(buf[0xA]).append('.').append(buf[0xB]);
//                ver.append(" (ICQ ");
//                makeVersion(ver, buf[0xC] & 0x7F, buf[0xD], buf[0xE], buf[0xF]);
//                ver.append(')');
//            }
//        } else if (cli == CLI_LICQ) {
//            makeVersion(ver, buf[0xC], buf[0xD] % 100, buf[0xE], -1);
//        } else if (cli == CLI_KOPETE) {
//            makeVersion(ver, buf[0xC], buf[0xD], buf[0xE], buf[0xF]);
//        } else if (cli == CLI_ANDRQ) {
//            ver.append((char) buf[0xC]).append('.').append((char) buf[0xB]);
//        } else if (cli == CLI_JIMM) {
//            ver.append(Util.byteArrayToString(buf, 5, 11));
//        } else if (cli == CLI_QIP) {
//            ver.append(Util.byteArrayToString(buf, 11, 5));
//        } else if (cli == CLI_MIP) {
//            ver.append(Util.byteArrayToString(buf, 4, 12));
//        } else if (cli == CLI_YAPP) {
//            ver.append(Util.byteArrayToString(buf, 8, 5));
//        } else if (cli == CLI_MCHAT) {
//            ver.append(Util.byteArrayToString(buf, 10, 6));
//        } else if (cli == CLI_VMICQ) {
//            ver.append(Util.byteArrayToString(buf, 6, 7));
//        }
//    }
}