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

## ZINS (Zeros Insert)

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/insrws.1217485/){:target="_blank"}

### About

Inserts 1 or more zeros to a sequence of n elements.

#### Inputs:

   - n - nr. of elements
   - x - element's group size
   - g - 0's group size

### Code

{% capture code %}
ZINS = LAMBDA(n, x, g,
    LET(
        t, TOCOL(
            IFNA(EXPAND(WRAPROWS(SEQUENCE(n), x), , g + x), 0)
        ),
        TAKE(t, XMATCH(n, t))
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}