---
type: Guides
category: Lambda
series: Lambda - General Purpose Library
date: 18/09/2022
toc: true
title: Array Transformation functions
description: 
layout: docs
---

# {{page.title}}
<time class="metadata" style="text-alstyleign:left"> {{page.type}} • {{page.category}} • {{page.date}}</time>

## ARRINS (Arrays Insert)

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/insrws.1217485/){:target="_blank"}

### About

"Intercalates" rows or columns of 2 arrays.

Calls [ABLNKINS](../lambda-library/lambda-ablnkins.html).

#### Inputs:

   - ar - array to receive the insert
   - br - array to be inserted
   - [rc] - rows/columns orientation, if omitted or 0, by rows, if 1 by columns
   - [h] - headers array

### More Info

*NOTE*: Arrays should be proportionate by nr. of elements and to share one dimension. 

### Code

{% capture code %}
ARRINS = LAMBDA(ar, br, [rc], [h],
    LET(
        a, IF(ar = "", "", ar),
        b, IF(br = "", "", br),
        o, IF(rc, -1, 1),
        k, o * COUNTA(a) / COUNTA(b),
        x, ABLNKINS(a, k),
        v, IFNA(IF(rc, HSTACK(x, ""), VSTACK(x, "")), ""),
        y, ABLNKINS(b, o * 1, -ABS(k)),
        z, IF(v = "", y, v),
        IF(AND(h = ""), z, IF(rc, HSTACK(h, z), VSTACK(h, z)))
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}