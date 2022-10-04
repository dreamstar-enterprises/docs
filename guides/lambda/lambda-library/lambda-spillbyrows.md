---
type: Guides
category: Lambda
series: Lambda - General Purpose Library
date: 18/09/2022
toc: true
title: Array 'By Element' functions
description: 
layout: docs
---

# {{page.title}}
<time class="metadata" style="text-alstyleign:left"> {{page.type}} • {{page.category}} • {{page.date}}</time>

## SPILLBYROWS

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/ascan.1183314/page-5#post-5858952){:target="_blank"}

### About

Applies a helper lambda function by row, on a 2-D array.

#### Inputs:

   - ar - array
   - fn - function lambda helper argument LAMBDA(x,fn(x))
   - [er] - error message argument, if the function delivers no results

### Code

{% capture code %}
SPILLBYROWS = LAMBDA(ar,fn,[er],
    LET(
        e, IF(ISOMITTED(er), NA(), er),
        a, IF(ar = "", "", ar),
        r, REDUCE(
            0,
            SEQUENCE(ROWS(a)),
            LAMBDA(v,i,
                LET(
                    x, INDEX(a, i, ),
                    y, IFERROR(
                        IF(
                            COLUMNS(x) = 1,
                            fn(INDEX(a, i, 1)),
                            fn(FILTER(x, x <> ""))
                        ),
                        ""
                    ),
                    VSTACK(v, y)
                )
            )
        ),
        d, DROP(IFNA(r, ""), 1),
        IF(AND(d = ""), e, d)
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}