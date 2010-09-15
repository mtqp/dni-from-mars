import random
import sys
import urllib

base_url = "http://chart.apis.google.com/chart?chs=1000x200&chbh=a&chxt=x&"

def generate_colours(n):
    hex_chars = [str(i) for i in range(5, 10)] + 'A B C D E F'.split()
    return ["".join([random.choice(hex_chars) for _ in range(6)]) for _ in range(n)]

def join(data):
    res = [[data[0], 0]]

    for t in data:
        if t == res[-1][0]:
            res[-1][1] += 1
        else:
            res.append([t, 1])

    return res

def graph(tdata, sched_name, ts_name):
    jdata = join(tdata)
    total = float(sum([t[1] for t in jdata]))

    data = [(t[0], int(round((t[1] / total) * 100))) for t in jdata]    
    stimes = [str(t[1]) for t in data]
    graph_url = base_url + "chd=t:%s&" % "|".join(stimes)

    names = list(set([t[0] for t in data]))
    ntasks = len(names)
    colour_list = generate_colours(ntasks)
    colours = dict(zip(names, colour_list[:len(names)]))
    scolours = [colours[p[0]] for p in data]
    graph_url += "cht=bhs&chco=%s&" % ",".join(scolours)

    snames = ["t%s,000000,%d,0,20" % (t[0], index) for index, t in enumerate(data)]
    graph_url += "chm=%s&" % "|".join(snames)

    total_time = sum([t[1] for t in jdata])
    slabels = [str(i) for i in range(0, total_time + 1, total_time / 10)]
    graph_url += "chxl=0:|%s" % "|".join(slabels)

    print "URL to generate graph:"
    print graph_url
#    urllib.urlretrieve(graph_url, "%s_%s_graph.png" % (sched_name, ts_name))

    return

"""
MAIN proc
"""
def read_timeline(dat_filename):

  linestring = open(dat_filename, "r").read()
  lines = linestring.split("\n")
  t = lines[0].strip().split()
  ticks = t[0]
  scheduler_name = t[1]
  task_set_name  = t[2]
  tdata          = lines[1:int(ticks)+1]

  graph(tdata, scheduler_name, task_set_name)

  return
