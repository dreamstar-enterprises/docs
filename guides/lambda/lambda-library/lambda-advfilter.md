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

## ADVFILTER

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/advflt.1168576/){:target="_blank"}

### About

Advanced Filter of any array, by any criteria, flexible boolean logic. 

#### Inputs:

  - a - array, regular, dynamic, table
  - clm - columns indexes for boolean multiplication, as constant integers horizontal array, 2, or {2,3}
  - crm - criteria text strings or numeric arguments for boolean multiplication, horizontal array of criteria corresponding to clm indexes {">3","quad"}
  - cla - columns indexes for boolean adding, as constant integers horizontal array, 2, or {2,3}
  - crm - criteria text strings or numeric arguments for boolean adding, horizontal array of criteria corresponding to cla indexes {">3","quad"}

#### More Info:

*NOTE:* providing more criteria than column indexes, will result in the extra criteria being ignored. Enough column indexes should be provided


### Code

{% capture code %}
// Helper tool Lambdas
// Boolean Multiplication
// https://www.mrexcel.com/board/threads/t_afm.1168573/
T_AFM = LAMBDA(a, cl, cr, p,
    LET(
        n, COLUMNS(cl),
        x, INDEX(cl, n),
        y, INDEX(cr, n),
        z, INDEX(a, , x),
        IF(
            n = 1,
            p * (COUNTIFS(z, z, z, y) > 0),
            p *
                T_AFM(
                    a,
                    INDEX(cl, SEQUENCE(, n - 1)),
                    INDEX(cr, SEQUENCE(, n - 1)),
                    COUNTIFS(z, z, z, y) > 0
                )
        )
    )
);

// Boolean Adding
// https://www.mrexcel.com/board/threads/t_afa.1168572/
T_AFA = LAMBDA(a, cl, cr, p,
    LET(
        n, COLUMNS(cl),
        x, INDEX(cl, n),
        y, INDEX(cr, n),
        z, INDEX(a, , x),
        IF(
            n = 1,
            p + (COUNTIFS(z, z, z, y) > 0),
            p +
                T_AFA(
                    a,
                    INDEX(cl, SEQUENCE(, n - 1)),
                    INDEX(cr, SEQUENCE(, n - 1)),
                    COUNTIFS(z, z, z, y) > 0
                )
        )
    )
);

ADVFILTER = LAMBDA(a, clm, crm, cla, cra,
    LET(
        tm, IF(SUM(clm) = 0, 1, T_AFM(a, clm, crm, 1)),
        ta, IF(SUM(cla) = 0, 1, T_AFA(a, cla, cra, 0)),
        FILTER(a, tm * ta)
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}