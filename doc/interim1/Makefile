all: compile removetemporaries

compile: removeartifacts
	pdflatex presentation.tex

clean: removeartifacts

removeartifacts:
	rm -f *pdf *dvi	

removetemporaries:
	rm -f *aux *toc *log *nav *snm *out

