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

## ABLNKINS

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/insrws.1217485/){:target="_blank"}

### About

Advance Insert, inserts blank rows/columns, can add row/column headers.

Calls [INSBLNKRWS](../lambda-library/lambda-insblnkrws.html) that calls [ZINS](../lambda-library/lambda-zins.html). 

100% cosmetics function, double transpose and headers stacking versatility. 

#### Inputs:

   - ar - array
   - [rc] - rows/clms group size, orientation given by its sign
   - rc < 0, column orientation, rc > 0 rows orientation, if omitted rc = 1
   - [g] - gap size, if omitted g = 1, if < 0 gap still inserted, but before 1st row or 1st clm
   - [h] - row/column headers array, if omitted no stacking

### Code

{% capture code %}
ABLNKINS = LAMBDA(ar, [rc], [g], [h],
    LET(
        a, IF(ar = "", "", ar),
        x, IF(rc, ABS(rc), 1),
        f, rc < 0,
        b, IF(f, TRANSPOSE(a), a),
        c, INSBLNKRWS(b, x, g),
        d, IF(f, TRANSPOSE(c), c),
        IF(AND(h = ""), d, IF(f, HSTACK(h, d), VSTACK(h, d)))
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}