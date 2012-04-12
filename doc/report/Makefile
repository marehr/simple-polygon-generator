TEMPORARIES=*aux *toc *log *blg *bbl *out *lol *lot *lof parts/*aux parts/*log
ARTIFACTS=*pdf *dvi *ps
LATEX_OPTS=-interaction=nonstopmode

all: clean compile removetemporaries

# TODO: Wirklich 3x latex?!
compile:
	latex ${LATEX_OPTS} report.tex
	bibtex8 report
	latex ${LATEX_OPTS} report.tex
	latex ${LATEX_OPTS} report.tex

pdf: compile removetemporaries
	dvips report.dvi
	ps2pdf report.ps
	rm -f *dvi *ps

clean:
	rm -f $(TEMPORARIES) $(ARTIFACTS)
	
removetemporaries:
	rm -f $(TEMPORARIES)
