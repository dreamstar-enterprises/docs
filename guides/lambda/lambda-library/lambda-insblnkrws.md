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

## INSBLNKRWS

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/insrws.1217485/){:target="_blank"}

### About

Inserts blank Rows. 

Calls [ZINS](../lambda-library/lambda-zins.html).

#### Inputs:

   - a - array
   - [r] - row's group size, if omitted r=1
   - [g] - gap size, if omitted g = 1, if < 0 gap inserted also before 1st row

### Code

{% capture code %}
INSBLNKRWS = LAMBDA(a, [r], [g],
    LET(
        x, MAX(1, r),
        y, IF(g, ABS(g), 1),
        q, ZINS(ROWS(a), x, y),
        s, IF(g < 0, VSTACK(SEQUENCE(y) ^ 0 - 1, q), q),
        b, INDEX(IF(a = "", "", a), s, SEQUENCE(, COLUMNS(a))),
        IF(s, b, "")
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}