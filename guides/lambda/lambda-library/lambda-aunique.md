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

## AUNIQUE

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/aunique.1164797/){:target="_blank"}

### About

Extracts unique values, horizontal (for each row) left aligned , vertical (for each column) top aligned, all (in a vertical array). 

Calls [AUNQSRT](../lambda-library/lambda-aunqsrt.html), and [AFLAT](../lambda-library/lambda-aflat.html).

#### Inputs:

  - a - required array
  - k - required -1 vertical, 0 all vertical, 1 horizontal

### Code

{% capture code %}
AUNIQUE = LAMBDA(a, k,
    LET(
        x, OR(k = {-1, 0, 1}),
        af, AFLAT(a, 1),
        au, UNIQUE(FILTER(af, af <> "")),
        IF(
            x,
            SWITCH(k, 0, au, 1, AUNQSRT(a, ), -1, TRANSPOSE(AUNQSRT(TRANSPOSE(a), ))),
            "-1 vert., 0 all vert , 1 horiz."
        )
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}