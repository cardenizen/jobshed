package us.mn.state.dot.mnroad
/**
 * Created by IntelliJ IDEA.
 * User: carr1den
 * Date: Jun 26, 2009
 * Time: 9:27:32 AM
 * To change this template use File | Settings | File Templates.
 */

public class MultiValuedMap {
  private def map = [:]

  def theMap() { return map }

    def put(String key, String value) {
      if (map[key] == null) {
        def v = []
        v.add(value)
        map.put(key,v)
      }
      else {
        map.get(key).add(value)
      }
    }

    def put(Long key, String value) {
      if (map[key] == null) {
        def v = []
        v.add(value)
        map.put(key,v)
      }
      else {
        map.get(key).add(value)
      }
    }

  def asList() {
    def aList = []
    map.each {k, v ->
      v.each {
        aList.add(k + " : " + it)
      }
    }
    return aList
  }

  def asList(String s) {
    def aList = []
    map.each {k, v ->
      if (k == s)
      v.each {
        aList.add(it)
      }
    }
    return aList
  }

  def getValueCount(key) {
    def l = map.get(key)
    if (l == null)
      return 0
    return l.size()
  }

  def String toString() {
    def l = []
    map.keySet().each {
      def rc = ""
      def val = getValue(it)
      rc += "${it}:"
      if (val instanceof String) {
        rc += val
      } else {
        rc += val.join(",").toString()
      }
      l << rc
    }
    return l.join("\n")
  }

  def getValue(key) {
    def l = map.get(key)
    if (l == null)
      return ""
    if (l.size() == 1) {
      return l[0]
    }
    else {
      return l
    }
  }


}