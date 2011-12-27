package us.mn.state.dot.mnroad;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import au.com.bytecode.opencsv.CSVWriter;
import org.apache.log4j.Logger;

public class TcCsvWriter {
static Logger log = Logger.getLogger(TcCsvWriter.class);

    public int writeResultSet (Connection conn, Map qm) throws Exception {
        int rc = 0;
        String query = (String)qm.get(TcDataQualityService.DATA_QUERY);
        String fileName = (String)qm.get(TcDataQualityService.FILE_NAME);
        ArrayList columnNames = (ArrayList)qm.get(TcDataQualityService.VALUE_COLUMN_NAMES);
        ArrayList fixedColumnNames = (ArrayList)qm.get(TcDataQualityService.FIXED_COLUMN_NAMES);
        if (query == null || fileName == null || columnNames.size()==0) {
            StringBuilder sb = new StringBuilder("Required parameter not found: ");
            if (query == null)
                sb.append(TcDataQualityService.DATA_QUERY);
            if (fileName == null)
                sb.append(TcDataQualityService.FILE_NAME);
            if (columnNames.size()==0)
                sb.append(TcDataQualityService.VALUE_COLUMN_NAMES);
            log.info(sb.toString());
            return 0;
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(query);
            List args = (List)qm.get(TcDataQualityService.ARGLIST);
            int ia = 1;
            for (Object arg : args) {
                if (arg instanceof Integer) {
                    ps.setInt(ia++, ((Integer)arg).intValue());
                }
                else if (arg instanceof java.sql.Date) {
                    ps.setDate(ia++, (java.sql.Date)arg);
                }
                else if (arg instanceof String) {
                    ps.setString(ia++, (String)arg);
                }
            }
            rs = ps.executeQuery();
            if (rs != null) {
                if (rs.isBeforeFirst()) { // true if the cursor is before the first row;
                    // false if the cursor is at any other position or the result set contains no rows
                    int colIdx = 0;
                    CSVWriter writer = null;
                    try {
                        System.out.print("Writing "+fileName);
                        writer = new CSVWriter(new FileWriter(fileName),CSVWriter.DEFAULT_SEPARATOR,CSVWriter.NO_QUOTE_CHARACTER, '\\');
                        ArrayList colNames = new ArrayList();
                        colNames.addAll(fixedColumnNames);
                        colNames.addAll(columnNames);
                        writer.writeNext(MrUtils.toArrayOfStrings(colNames));
                        while (rs.next()) {
                            colIdx = 1;
                            int cell = rs.getInt(colIdx++);
                            Date day = rs.getDate(colIdx++);
                            int hour = rs.getInt(colIdx++);
                            int qhr = rs.getInt(colIdx++);
                            int minute = rs.getInt(colIdx++);
                            ValueData vd = null;
                            if (TcDataQualityService.includeTcDataQualityFlag) {
                                vd = new ValueData(cell,day,hour,qhr,minute
                                    ,rs.getString(colIdx++)
                                );
                            } else {
                                vd = new ValueData(cell,day,hour,qhr,minute
                                );
                            }
                            for (int i=0;i < columnNames.size(); i++) {
                                vd.addValue((String)columnNames.get(i), rs.getBigDecimal(colIdx+i));
                            }
                            if (vd.hasValues()) {
                                writer.writeNext(vd.toStringArray());
                                rc++;
                            }
                        }
                        System.out.print(".");
                    } catch (IOException ioe) {
                        log.error(String.format("IOException: %s",ioe.getMessage()));
                        ioe.printStackTrace();
                        if (writer != null) {
                            writer.flush();
                            writer.close();
                            writer = null;
                        }
                    } catch (Exception e) {
                        log.error(String.format("Exception: %s",e.getMessage()));
                        e.printStackTrace();
                        if (writer != null) {
                            writer.flush();
                            writer.close();
                            writer = null;
                        }
                    }   finally {
                        if (writer != null) {
                            writer.flush();
                            writer.close();
                        }
                    }

                }
            }
        } catch (SQLException sqle) {
            log.error(String.format("SQLException: %s\nQuery: %s",sqle.getMessage(),query));
            throw new Exception(sqle.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) { /*ignored*/ }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) { /*ignored*/ }
            }
        }
        System.out.println(" done.");
        return rc;
    }
}
